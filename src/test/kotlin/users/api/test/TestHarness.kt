package users.api.test

import java.io.Closeable
import users.api.Application

class TestHarness : Closeable {

    val application = Application()

    val baseUri by lazy {
        "http://localhost:${application.server.config.port}"
    }

    val client by lazy {
        TestClient(baseUri)
    }

    init {
        application.start()
    }

    override fun close() {
        application.close()
        client.close()
    }
}
