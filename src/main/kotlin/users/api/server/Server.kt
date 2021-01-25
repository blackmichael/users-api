package users.api.server

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.Closeable
import java.time.Duration
import mu.KotlinLogging
import users.api.domain.service.UsersService
import users.api.server.handler.healthHandler
import users.api.server.handler.usersHandler

/**
 * Configures the application's HTTP server and defines all endpoints for it.
 */
class Server(val config: Config, private val usersService: UsersService) : Closeable {
    data class Config(
        val port: Int,
        val host: String
    )

    companion object {
        val logger = KotlinLogging.logger {}
    }

    private val server: ApplicationEngine = embeddedServer(Netty, config.port, config.host) {
        // request/response serde
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                registerModule(JavaTimeModule())
                propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
            }
        }

        // exception handling for both expected and unexpected errors
        install(StatusPages) {
            exception<ResourceNotFoundException> {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to it.message))
            }
            exception<BadRequestException> {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to it.message))
            }
            exception<Throwable> {
                logger.error { "server encountered an uncaught error: ${it.message}" }
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "something went wrong"))
            }
        }

        // handlers
        healthHandler()
        usersHandler(usersService)
    }

    fun start() {
        logger.info("starting server")
        server.start(false)
    }

    override fun close() {
        logger.info("shutting down server")
        server.stop(
            gracePeriodMillis = Duration.ofSeconds(1).toMillis(),
            timeoutMillis = Duration.ofSeconds(2).toMillis()
        )
    }
}
