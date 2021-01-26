package users.api.domain.model

import java.time.Instant

/**
 * Represents a user domain model.
 */
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val isTest: Boolean
)

/**
 * Represents one user liking another at a specific point in time.
 */
data class UserLike(
    val likedUserId: String,
    val likedByUserId: String,
    val likedAt: Instant
)
