package users.api

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import users.api.test.TestHarness
import users.api.test.shouldBeJson

class ApplicationTest {
    val harness = TestHarness()

    @Test
    fun `test health check`() {
        val response = harness.client.get("/health")
        response.status shouldBe 200
        response.body shouldBeJson mapOf("status" to "UP")
    }
}
