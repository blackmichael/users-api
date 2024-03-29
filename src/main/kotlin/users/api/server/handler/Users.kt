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

/**
 * Defines user endpoints for the Application receiver.
 *
 * @param usersService domain service for handling user logic
 */
fun Application.usersHandler(usersService: UsersService) {
    val userIdParamName = "userId"

    val pageQueryParamName = "page"
    val perPageQueryParamName = "per_page"

    routing {

        /**
         * Handler logic for
         *      GET /users/:userId
         */
        get("/users/{$userIdParamName}") {
            val userId = call.parameters[userIdParamName] ?: throw BadRequestException("missing user id param")

            when (val user = usersService.getUser(userId)) {
                null -> throw ResourceNotFoundException("user does not exist")
                else -> call.respond(HttpStatusCode.OK, user)
            }
        }

        /**
         * Handler logic for
         *      POST /user
         */
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

        /**
         * Handler logic for
         *      POST /users/:userId/likes
         */
        post("/users/{$userIdParamName}/likes") {
            val request = try {
                call.receive<LikeUserRequest>()
            } catch (e: Exception) {
                throw BadRequestException("invalid or missing request body")
            }

            val likedUserId = call.parameters[userIdParamName] ?: throw BadRequestException("missing user id param")
            val response = usersService.likeUser(likedUserId, request.likedByUserId)

            call.respond(HttpStatusCode.Created, response)
        }

        /**
         * Handler logic for
         *      GET /users/:userId/likes
         */
        get("/users/{$userIdParamName}/likes") {
            val likedUserId = call.parameters[userIdParamName] ?: throw BadRequestException("missing user id param")
            val page = try {
                call.request.queryParameters[pageQueryParamName]?.toInt()
            } catch (e: NumberFormatException) {
                throw BadRequestException("page must be numerical")
            }
            val perPage = try {
                call.request.queryParameters[perPageQueryParamName]?.toInt()
            } catch (e: NumberFormatException) {
                throw BadRequestException("per_page must be numerical")
            }

            val likes = usersService.getUserLikes(likedUserId, page, perPage)

            call.respond(HttpStatusCode.OK, likes)
        }
    }
}

/**
 * Request schema for POST /users
 */
data class CreateUserRequest(
    val firstName: String,
    val lastName: String,
    val isTest: Boolean = false
) {

    /**
     * Mapping function to convert a request into a [User] domain object.
     */
    fun toUser(): User =
        User(
            id = UUID.randomUUID().toString(),
            firstName = this.firstName,
            lastName = this.lastName,
            isTest = this.isTest
        )
}

/**
 * Request schema for POST /users/:userId/likes
 */
data class LikeUserRequest(
    val likedByUserId: String
)
