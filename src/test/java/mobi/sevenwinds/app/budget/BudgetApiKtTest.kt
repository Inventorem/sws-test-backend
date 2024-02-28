package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
    }

    @Test
    fun testBudgetPagination() {
        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assertions.assertEquals(5, response.total)
                Assertions.assertEquals(3, response.items.size)
                Assertions.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assertions.assertEquals(30, response.items[0].amount)
                Assertions.assertEquals(5, response.items[1].amount)
                Assertions.assertEquals(400, response.items[2].amount)
                Assertions.assertEquals(100, response.items[3].amount)
                Assertions.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecord(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecord(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }

    @Test
    fun addWithNonexistentAuthor() {
        val record = BudgetRecord(2024, 2, 1000, BudgetType.Приход, 100500)
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .let { Assertions.assertEquals(it.statusCode, 404) }
    }

    @Test
    fun addWithExistedAuthor() {
        val expcetedAuthor = addRecord(CreateAuthorRequest("Бюджетный Бюджет Бюджетович"))
        val expectedBudgetRecord = BudgetRecord(2024, 2, 1000, BudgetType.Приход, expcetedAuthor.id)
        addRecord(expectedBudgetRecord)
    }

    @Test
    fun getStatsByOneAuthorWithDifferentCases() {
        val expectedAuthor1 = addRecord(CreateAuthorRequest("Бюджетный Бюджет Бюджетович"))
        val expectedAuthor2 = addRecord(CreateAuthorRequest("БюДжЕтНый БюДжЕТ БюДжЕтОвИч"))
        val expectedBudgetRecords = listOf(expectedAuthor1, expectedAuthor2)
            .map {
                addRecord(BudgetRecord(2024, 2, 1000, BudgetType.Приход, it.id))
            }
        RestAssured.given()
            .queryParam("limit", 100)
            .queryParam("offset", 0)
            .queryParam("authorName", "Бюджетный Бюджет Бюджетович")
            .get("/budget/year/2024/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)
                Assertions.assertEquals(expectedBudgetRecords, response.items)
            }

    }


    private fun addRecord(record: BudgetRecord): BudgetRecord{
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecord>().let { response ->
                Assertions.assertEquals(record, response)
                return response
            }
    }

    private fun addRecord(record: CreateAuthorRequest): AuthorRecord {
        RestAssured.given()
            .jsonBody(record)
            .post("/author/add")
            .toResponse<AuthorRecord>().let { response ->
                println(response)
                Assertions.assertEquals(record.fullName, response.fullName)
                return response
            }
    }
}