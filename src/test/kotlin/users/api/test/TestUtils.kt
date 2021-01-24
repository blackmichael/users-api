package users.api.test

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe

val jackson: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

infix fun String.shouldBeJson(other: Any) =
    this shouldBe jackson.writeValueAsString(other)

infix fun Any.shouldBeJson(other: Any) =
    jackson.writeValueAsString(this) shouldBe jackson.writeValueAsString(other)
