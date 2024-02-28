package mobi.sevenwinds.app.budget

import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRecord, CreateAuthorRequest>(info("Добавить автора")) { _, body ->
            respond(AuthorService.addRecord(body))
        }
        route("/{id}").get<AuthorParam, AuthorRecord>(info("Получить автора по айди")){
            respond(AuthorService.getRecord(it))
        }
    }
}

data class AuthorRecord(
    val id : Int,
    val fullName: String,
    val createdAt: String
)

data class CreateAuthorRequest(
    val fullName: String,
)

data class AuthorParam(
    @PathParam("Айди автора") val id: Int
)