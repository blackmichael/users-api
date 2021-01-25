package users.api.test

import java.io.Closeable
import java.net.ServerSocket
import users.api.Application

class TestHarness : Closeable {

    val openPort: Int = ServerSocket(0)
        .use {
            it.reuseAddress = true
            it.localPort
        }
        .also {
            System.setProperty("http.server.port", "$it")
        }

    val application = Application()

    val baseUri by lazy {
        "http://localhost:${application.server.config.port}"
    }

    val client by lazy {
        TestClient(baseUri)
    }

    val postgresHelper by lazy {
        PostgresHelper(application.postgresService.context)
    }

    init {
        application.start()
    }

    override fun close() {
        application.close()
        client.close()
    }
}
