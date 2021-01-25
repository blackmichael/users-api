package users.api.test

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.io.Closeable
import kotlinx.coroutines.runBlocking

class TestClient(private val baseUri: String) : Closeable {

    private val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer() {
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
            }
        }
        expectSuccess = false
    }

    fun get(path: String): Response =
        runBlocking {
            val response: HttpResponse = client.get("$baseUri$path")
            Response(response.status.value, response.readText())
        }

    fun post(path: String, body: Any): Response =
        runBlocking {
            val response: HttpResponse = client.post("$baseUri$path") {
                contentType(ContentType.Application.Json)
                this.body = body
            }
            Response(response.status.value, response.readText())
        }

    override fun close() {
        client.close()
    }
}

data class Response(val status: Int, val body: String)
