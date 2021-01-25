package users.api.postgres

import org.jooq.Record
import org.jooq.impl.DSL
import users.api.domain.model.User

/**
 * Converts a PostgreSQL row [Record] into a [User].
 */
fun Record.toUser(): User =
    User(
        id = this[DSL.field("id", String::class.java)],
        firstName = this[DSL.field("first_name", String::class.java)],
        lastName = this[DSL.field("last_name", String::class.java)],
        isTest = this[DSL.field("is_test", Boolean::class.java)]
    )
