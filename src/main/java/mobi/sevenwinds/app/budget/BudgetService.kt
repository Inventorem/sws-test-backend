package mobi.sevenwinds.app.budget

import io.ktor.features.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId?.let {
                    AuthorEntity.findById(it)?.id
                        ?: throw NotFoundException("Author with id ${body.authorId} was not found")
                }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {

            val query: Query = buildQuery(param)

            val total = query.count()

            val sumByType = BudgetEntity
                .wrapRows(query)
                .map { it.toResponse() }
                .groupBy { it.type.name }
                .mapValues { it.value.sumOf { v -> v.amount } }

            val paginatedAndSortedQuery = query.limit(param.limit, param.offset)
                .orderBy(
                    BudgetTable.month to SortOrder.ASC,
                    BudgetTable.amount to SortOrder.DESC
                )

            val allByYearPaginated = BudgetEntity
                .wrapRows(paginatedAndSortedQuery)
                .map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = allByYearPaginated
            )
        }
    }

    private fun buildQuery(
        params: BudgetYearParam
    )  = params.authorName?.let {
            (BudgetTable innerJoin AuthorTable)
                .select {
                    (BudgetTable.year eq params.year) and
                    (AuthorTable.fullName.lowerCase() like "%${params.authorName.toLowerCase()}%")
                }
        } ?: BudgetTable.select { BudgetTable.year eq params.year }
}