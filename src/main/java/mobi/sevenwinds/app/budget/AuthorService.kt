package mobi.sevenwinds.app.budget

import io.ktor.features.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addRecord(body: CreateAuthorRequest): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fullName = body.fullName
                this.createdAt = DateTime.now()
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getRecord(param : AuthorParam) : AuthorRecord = withContext(Dispatchers.IO){
        transaction {
            val id = param.id
            return@transaction AuthorEntity.findById(id)?.toResponse()
                ?: throw NotFoundException("Author with id $id is not found")
        }
    }
}