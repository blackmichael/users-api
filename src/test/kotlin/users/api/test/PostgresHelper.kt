package users.api.test

import org.jooq.DSLContext
import org.jooq.impl.DSL

class PostgresHelper(val context: DSLContext) {

    fun deleteAllUsers() {
        context.truncate(DSL.table("users"))
    }
}
