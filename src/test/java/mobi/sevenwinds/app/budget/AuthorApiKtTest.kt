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

class AuthorApiKtTest : ServerTest() {
    @BeforeEach
    internal fun setUp() {
        transaction {
            BudgetTable.deleteAll()
            AuthorTable.deleteAll()
        }
    }

    @Test
    fun createAuthorSuccess() {
        addRecord(CreateAuthorRequest("Иванов Иван Иваныч"))
        addRecord(CreateAuthorRequest("Иванов Петр Иваныч"))
        addRecord(CreateAuthorRequest("Иванов Иван Сергеевич"))
    }

    @Test
    fun getAuthorSuccess() {
        val expectedId = addRecord(CreateAuthorRequest("Иванов Иван Иваныч")).id
        getRecord(expectedId).let { Assertions.assertEquals(it.id, expectedId) }
    }

    @Test
    fun getAuthorNotFound() {
        RestAssured.given()
            .pathParam("id", 100500)
            .get("/author/{id}")
            .let { Assertions.assertEquals(it.statusCode, 404) }
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

    private fun getRecord(id: Int): AuthorRecord {
        RestAssured.given()
            .pathParam("id", id)
            .get("/author/{id}")
            .toResponse<AuthorRecord>().let { response ->
                println(response)
                return response
            }
    }
}