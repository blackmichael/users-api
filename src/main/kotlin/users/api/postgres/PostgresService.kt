package users.api.postgres

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.Closeable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import users.api.domain.model.User

/**
 * Persistence layer service responsible for maintaining the PostgreSQL integration and applying migrations.
 */
class PostgresService(val config: Config) : Closeable {
    data class Config(
        val threadPoolSize: Int,
        val applyMigrations: Boolean = true,
        val dataSource: DataSourceConfig
    )

    data class DataSourceConfig(
        val username: String,
        val password: String,
        val serverName: String,
        val portNumber: Int,
        val databaseName: String,
        val sslMode: String
    )

    companion object {
        val logger = KotlinLogging.logger { }
    }

    private val dataSource = HikariDataSource(
        HikariConfig().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("user", config.dataSource.username)
            addDataSourceProperty("password", config.dataSource.password)
            addDataSourceProperty("serverName", config.dataSource.serverName)
            addDataSourceProperty("portNumber", config.dataSource.portNumber)
            addDataSourceProperty("databaseName", config.dataSource.databaseName)
            addDataSourceProperty("sslMode", config.dataSource.sslMode)
        }
    )

    private val migrations by lazy {
        Flyway.configure()
            .dataSource(dataSource)
            .load()
    }

    val context: DSLContext by lazy {
        DSL.using(dataSource, SQLDialect.POSTGRES)
    }

    init {
        if (config.applyMigrations) {
            logger.info("applying migrations")
            migrations.migrate()
            logger.info("applied ${migrations.info().applied().size} migrations")
        }
    }

    /**
     * Inserts the given user into the database.
     *
     * @param user
     */
    suspend fun createUser(user: User) =
        suspendedTxn("insert user") {
            insertInto(
                DSL.table("users"),
                DSL.field("id"),
                DSL.field("first_name"),
                DSL.field("last_name"),
                DSL.field("is_test")
            )
                .values(
                    user.id,
                    user.firstName,
                    user.lastName,
                    user.isTest
                )
                .execute()
        }

    /**
     * Selects the user with the given ID, if it exists.
     *
     * @param id
     *
     * @return null if no user exists with that ID
     */
    suspend fun getUser(id: String): User? =
        suspendedTxn("get user by id") {
            select()
                .from(DSL.table("users"))
                .where(DSL.field("id").eq(id))
                .fetchOne()
                ?.toUser()
        }

    /**
     * Logs the transaction description and executes it within the [Dispatchers.IO] coroutine context.
     *
     * @param description
     * @param block
     *
     * @return the result of [block]
     */
    private suspend fun <T> suspendedTxn(description: String, block: DSLContext.() -> T): T =
        withContext(Dispatchers.IO) {
            logger.debug(description)
            context.transactionResult { configuration ->
                DSL.using(configuration).block()
            }
        }

    override fun close() {
        logger.info("closing postgres service")
        dataSource.close()
    }
}
