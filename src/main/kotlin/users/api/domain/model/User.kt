package users.api.domain.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val isTest: Boolean
)
