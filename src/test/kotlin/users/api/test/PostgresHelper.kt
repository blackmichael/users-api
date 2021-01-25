package users.api.test

import org.jooq.DSLContext
import org.jooq.impl.DSL

class PostgresHelper(private val context: DSLContext) {

    fun deleteAll() {
        context.truncate(DSL.table("users")).cascade().execute()
    }
}
