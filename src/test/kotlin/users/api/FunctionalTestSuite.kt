package users.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.shouldBe
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import users.api.domain.model.User
import users.api.test.TestHarness
import users.api.test.jackson
import users.api.test.randomString
import users.api.test.shouldBeJson

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionalTestSuite {
    private val harness = TestHarness()

    @BeforeEach
    fun beforeEach() {
        harness.postgresHelper.deleteAllUsers()
    }

    @Test
    fun `test health check`() {
        val response = harness.client.get("/health")
        response.status shouldBe 200
        response.body shouldBeJson mapOf("status" to "UP")
    }

    @Test
    fun `test create user - 200`() {
        val request = mapOf(
            "first_name" to "Michael",
            "last_name" to "Black"
        )

        val response = harness.client.post("/users", request)
        response.status shouldBe 201

        // test individual attributes since we don't know the id
        val responseBody = jackson.readValue<User>(response.body)
        responseBody.firstName shouldBe request["first_name"]
        responseBody.lastName shouldBe request["last_name"]
        responseBody.isTest shouldBe false
    }

    @Test
    fun `test create test user - 200`() {
        val request = mapOf(
            "first_name" to "Michael",
            "last_name" to "Black",
            "is_test" to true
        )

        val response = harness.client.post("/users", request)
        response.status shouldBe 201

        // test individual attributes since we don't know the id
        val responseBody = jackson.readValue<User>(response.body)
        responseBody.firstName shouldBe request["first_name"]
        responseBody.lastName shouldBe request["last_name"]
        responseBody.isTest shouldBe true
    }

    @Test
    fun `test create user - 400 missing required attribute`() {
        val request = mapOf(
            "first_name" to "Michael"
        )

        val response = harness.client.post("/users", request)
        response.status shouldBe 400
        response.body shouldBeJson mapOf("message" to "invalid or missing request body")
    }

    @Test
    fun `test get user - 200`() {
        val user = createRandomUser()

        val response = harness.client.get("/users/${user.id}")
        response.status shouldBe 200
        response.body shouldBeJson user
    }

    @Test
    fun `test get user - 404`() {
        val response = harness.client.get("/users/${UUID.randomUUID()}")
        response.status shouldBe 404
        response.body shouldBeJson mapOf("message" to "user does not exist")
    }

    private fun createRandomUser(request: Map<String, Any>? = null): User {
        val req = request ?: mapOf(
            "first_name" to randomString(),
            "last_name" to randomString()
        )

        val response = harness.client.post("/users", req)
        response.status shouldBe 201
        return jackson.readValue(response.body)
    }
}
