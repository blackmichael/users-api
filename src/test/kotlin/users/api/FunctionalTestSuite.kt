package users.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import users.api.domain.model.User
import users.api.domain.model.UserLike
import users.api.test.TestHarness
import users.api.test.jackson
import users.api.test.randomString
import users.api.test.shouldBeJson

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionalTestSuite {
    private val harness = TestHarness()

    @BeforeEach
    fun beforeEach() {
        harness.postgresHelper.deleteAll()
    }

    @AfterAll
    fun afterAll() {
        harness.close()
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

    @Test
    fun `test create like - 200`() {
        val user1 = createRandomUser()
        val user2 = createRandomUser()
        val request = mapOf(
            "liked_by_user_id" to user2.id
        )

        val response = harness.client.post("/users/${user1.id}/likes", request)
        response.status shouldBe 201

        val responseBody = jackson.readValue<UserLike>(response.body)
        responseBody.likedUserId shouldBe user1.id
        responseBody.likedByUserId shouldBe user2.id
        responseBody.likedAt shouldNotBe null
    }

    @Test
    fun `test create like - 400 invalid request body`() {
        val user1 = createRandomUser()
        val user2 = createRandomUser()
        val request = mapOf(
            "liked_user_id" to user2.id
        )

        val response = harness.client.post("/users/${user1.id}/likes", request)
        response.status shouldBe 400
        response.body shouldBeJson mapOf("message" to "invalid or missing request body")
    }

    @Test
    fun `test create like - 400 user liked themself`() {
        val user1 = createRandomUser()
        val request = mapOf(
            "liked_by_user_id" to user1.id
        )

        val response = harness.client.post("/users/${user1.id}/likes", request)
        response.status shouldBe 400
        response.body shouldBeJson mapOf("message" to "users cannot like themselves")
    }

    @Test
    fun `test create like - 404 missing liked user`() {
        val user1 = createRandomUser()
        val request = mapOf(
            "liked_by_user_id" to user1.id
        )

        val response = harness.client.post("/users/${UUID.randomUUID()}/likes", request)
        response.status shouldBe 404
        response.body shouldBeJson mapOf("message" to "liked user was not found")
    }

    @Test
    fun `test create like - 404 missing liked by user`() {
        val user1 = createRandomUser()
        val request = mapOf(
            "liked_by_user_id" to UUID.randomUUID().toString()
        )

        val response = harness.client.post("/users/${user1.id}/likes", request)
        response.status shouldBe 404
        response.body shouldBeJson mapOf("message" to "liked by user was not found")
    }

    @Test
    fun `test get likes - 200 default pagination`() {
        val user1 = createRandomUser()
        val user2 = createRandomUser()
        val user3 = createRandomUser()

        createLike(user1, user3)
        // create a delay between likes
        runBlocking { delay(10) }
        createLike(user1, user2)

        val response = harness.client.get("/users/${user1.id}/likes")
        response.status shouldBe 200

        val responseBody = jackson.readValue<List<User>>(response.body)
        responseBody shouldBeJson listOf(user3, user2)
    }

    @Test
    fun `test get likes - 200 paginated results`() {
        val user = createRandomUser()
        val users = (1..95).map { createRandomUser() }
        users.forEach {
            createLike(user, it)
                .also {
                    runBlocking { delay(10) }
                }
        }

        // first page
        val response1 = harness.client.get("/users/${user.id}/likes?page=0&per_page=20")
        response1.status shouldBe 200
        val responseBody1 = jackson.readValue<List<User>>(response1.body)
        responseBody1.size shouldBe 20

        // second page
        val response2 = harness.client.get("/users/${user.id}/likes?page=1&per_page=20")
        response2.status shouldBe 200
        val responseBody2 = jackson.readValue<List<User>>(response2.body)
        responseBody2.size shouldBe 20

        // third page
        val response3 = harness.client.get("/users/${user.id}/likes?page=2&per_page=20")
        response3.status shouldBe 200
        val responseBody3 = jackson.readValue<List<User>>(response3.body)
        responseBody3.size shouldBe 20

        // fourth page
        val response4 = harness.client.get("/users/${user.id}/likes?page=3&per_page=20")
        response4.status shouldBe 200
        val responseBody4 = jackson.readValue<List<User>>(response4.body)
        responseBody4.size shouldBe 20

        // fifth page
        val response5 = harness.client.get("/users/${user.id}/likes?page=4&per_page=20")
        response5.status shouldBe 200
        val responseBody5 = jackson.readValue<List<User>>(response5.body)
        // remaining likes
        responseBody5.size shouldBe 15

        val totalResponse = (responseBody1 + responseBody2 + responseBody3 + responseBody4 + responseBody5)
        totalResponse shouldContainExactlyInAnyOrder users
    }

    @Test
    fun `test get likes - 400 invalid page query param`() {
        val response1 = harness.client.get("/users/${UUID.randomUUID()}/likes?page=a&per_page=10")
        response1.status shouldBe 400
        response1.body shouldBeJson mapOf("message" to "page must be numerical")

        val response2 = harness.client.get("/users/${UUID.randomUUID()}/likes?page=-1&per_page=10")
        response2.status shouldBe 400
        response2.body shouldBeJson mapOf("message" to "page must not be negative")
    }

    @Test
    fun `test get likes - 400 invalid per_page query param`() {
        val response1 = harness.client.get("/users/${UUID.randomUUID()}/likes?page=0&per_page=a")
        response1.status shouldBe 400
        response1.body shouldBeJson mapOf("message" to "per_page must be numerical")

        val response2 = harness.client.get("/users/${UUID.randomUUID()}/likes?page=0&per_page=0")
        response2.status shouldBe 400
        response2.body shouldBeJson mapOf("message" to "per_page must be positive")

        val response3 = harness.client.get("/users/${UUID.randomUUID()}/likes?page=0&per_page=-1")
        response3.status shouldBe 400
        response3.body shouldBeJson mapOf("message" to "per_page must be positive")
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

    private fun createLike(likedUser: User, likedByUser: User): UserLike {
        val req = mapOf("liked_by_user_id" to likedByUser.id)
        val response = harness.client.post("/users/${likedUser.id}/likes", req)
        response.status shouldBe 201
        return jackson.readValue(response.body)
    }
}
