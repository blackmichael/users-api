package users.api.server.handler

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import java.util.UUID
import users.api.domain.model.User
import users.api.domain.service.UsersService
import users.api.server.BadRequestException
import users.api.server.ResourceNotFoundException

fun Application.usersHandler(usersService: UsersService) {
    val userIdParamName = "userId"

    routing {
        get("/users/{$userIdParamName}") {
            val userId = call.parameters[userIdParamName] ?: throw IllegalArgumentException("missing user id param")

            when (val user = usersService.getUser(userId)) {
                null -> throw ResourceNotFoundException("user does not exist")
                else -> call.respond(HttpStatusCode.OK, user)
            }
        }

        post("/users") {
            val request = try {
                call.receive<CreateUserRequest>()
            } catch (e: Exception) {
                throw BadRequestException("invalid or missing request body")
            }

            val newUser = request.toUser()
            usersService.createUser(newUser)

            call.respond(HttpStatusCode.Created, newUser)
        }
    }
}

data class CreateUserRequest(
    val firstName: String,
    val lastName: String,
    val isTest: Boolean = false
)

fun CreateUserRequest.toUser(): User =
    User(
        id = UUID.randomUUID().toString(),
        firstName = this.firstName,
        lastName = this.lastName,
        isTest = this.isTest
    )
