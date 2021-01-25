package users.api.test

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import kotlin.random.Random

val jackson: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
    .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

infix fun String.shouldBeJson(other: Any) =
    this shouldBe jackson.writeValueAsString(other)

infix fun Any.shouldBeJson(other: Any) =
    jackson.writeValueAsString(this) shouldBe jackson.writeValueAsString(other)

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun randomString(length: Int = 20): String =
    (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
