package users.api.domain.service

import users.api.domain.model.User
import users.api.postgres.PostgresService

/**
 * Domain layer service responsible for maintaining business logic and orchestrating I/O.
 */
class UsersService(private val postgresService: PostgresService) {

    /**
     * Gets a user by a given ID.
     *
     * @param id
     *
     * @return null if no user exists with that ID.
     */
    suspend fun getUser(id: String): User? {
        return postgresService.getUser(id)
    }

    /**
     * Create a user.
     *
     * @param user
     */
    suspend fun createUser(user: User) {
        postgresService.createUser(user)
    }
}
