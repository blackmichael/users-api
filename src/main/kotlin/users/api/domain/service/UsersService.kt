package users.api.domain.service

import java.time.Instant
import mu.KotlinLogging
import users.api.domain.model.User
import users.api.domain.model.UserLike
import users.api.postgres.PostgresService
import users.api.server.BadRequestException
import users.api.server.ResourceNotFoundException

/**
 * Domain layer service responsible for maintaining business logic and orchestrating I/O.
 */
class UsersService(private val postgresService: PostgresService) {

    companion object {
        val logger = KotlinLogging.logger { }
    }

    /**
     * Gets a user by a given ID.
     *
     * @param id
     *
     * @return null if no user exists with that ID.
     */
    suspend fun getUser(id: String): User? {
        logger.debug { "getting user for ID=$id" }

        return postgresService.getUser(id)
    }

    /**
     * Create a user.
     *
     * @param user
     */
    suspend fun createUser(user: User) {
        logger.debug { "creating new user" }

        postgresService.createUser(user)
    }

    /**
     * Creates a like between two users.
     *
     * @param likedUserId the user ID of the liked user
     * @param likedByUserId the user ID of the user who likes another user
     */
    suspend fun likeUser(likedUserId: String, likedByUserId: String): UserLike {
        logger.debug { "creating like between users" }

        if (likedUserId == likedByUserId) {
            throw BadRequestException("users cannot like themselves")
        }

        getUser(likedUserId) ?: throw ResourceNotFoundException("liked user was not found")
        getUser(likedByUserId) ?: throw ResourceNotFoundException("liked by user was not found")

        val likedAt = Instant.now()
        val userLike = UserLike(likedUserId, likedByUserId, likedAt)

        postgresService.createLike(userLike)

        return userLike
    }

    /**
     * Gets a list of users who have liked a given user.
     *
     * @param likedUserId the user ID of the user in question
     *
     * @return a list of users who have liked the given user, possibly empty
     */
    suspend fun getUserLikes(likedUserId: String, page: Int?, perPage: Int?): List<User> {
        logger.debug { "getting user likes" }

        page?.let {
            if (it < 0) {
                throw BadRequestException("page must not be negative")
            }
        }

        perPage?.let {
            if (it <= 0) {
                throw BadRequestException("per_page must be positive")
            }
        }

        getUser(likedUserId) ?: throw ResourceNotFoundException("liked user was not found")

        val defaultedPage = page ?: 0
        val defaultedPerPage = perPage ?: 20

        return postgresService.getLikes(likedUserId, defaultedPage, defaultedPerPage)
    }
}
