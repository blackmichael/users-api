package users.api.postgres

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.Closeable
import java.sql.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import users.api.domain.model.User
import users.api.domain.model.UserLike

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

        private val usersTable = DSL.table("users")
        private val idField = DSL.field("id", String::class.java)
        private val firstNameField = DSL.field("first_name", String::class.java)
        private val lastNameField = DSL.field("last_name", String::class.java)
        private val isTestField = DSL.field("is_test", Boolean::class.java)

        private val likesTable = DSL.table("likes")
        private val likedUserIdField = DSL.field("liked_user_id", String::class.java)
        private val likedByUserIdField = DSL.field("liked_by_user_id", String::class.java)
        private val likedAtField = DSL.field("liked_at", Timestamp::class.java)
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
                usersTable,
                idField,
                firstNameField,
                lastNameField,
                isTestField
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
                .from(usersTable)
                .where(idField.eq(id))
                .fetchOne()
                ?.toUser()
        }

    /**
     * Inserts a "like" which represents a relationship between two records in the users table.
     *
     * @param userLike a representation of when a user liked another user
     */
    suspend fun createLike(userLike: UserLike) {
        suspendedTxn("insert like") {
            insertInto(
                likesTable,
                likedUserIdField,
                likedByUserIdField,
                likedAtField
            )
                .values(
                    userLike.likedUserId,
                    userLike.likedByUserId,
                    Timestamp(userLike.likedAt.toEpochMilli())
                )
                .execute()
        }
    }

    /**
     * Selects all of the users who have liked the given user ID.
     *
     * @param likedUserId
     *
     * @return a list of users
     */
    suspend fun getLikes(likedUserId: String): List<User> =
        suspendedTxn("select liked by users") {
            select(
                idField,
                firstNameField,
                lastNameField,
                isTestField
            )
                .from(
                    likesTable
                        .join(usersTable)
                        .on(likedByUserIdField.eq(idField))
                )
                .where(likedUserIdField.eq(likedUserId))
                .orderBy(likedAtField)
                .fetch()
                .map {
                    it.toUser()
                }
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
