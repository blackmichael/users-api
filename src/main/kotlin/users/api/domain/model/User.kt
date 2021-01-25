package users.api.domain.model

import java.time.Instant

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val isTest: Boolean
)

data class UserLike(
    val likedUserId: String,
    val likedByUserId: String,
    val likedAt: Instant
)
