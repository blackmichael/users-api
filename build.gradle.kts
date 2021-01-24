import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.4.20"

    application

    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

application {
    mainClass.set("users.api.ApplicationKt")
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    implementation("io.ktor:ktor-server-netty:1.4.3")
    implementation("io.ktor:ktor-jackson:1.4.3")
    implementation("io.github.config4k:config4k:0.4.2")

    implementation("io.github.microutils:kotlin-logging:1.12.0")
    implementation("org.slf4j:slf4j-api:1.7.29")
    implementation("ch.qos.logback:logback-classic:1.1.7")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.ktor:ktor-client-apache:1.4.3")
    implementation("io.ktor:ktor-client-jackson:1.4.3")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.3.2")
}

val build: DefaultTask by tasks
val shadowJar: ShadowJar by tasks

build.dependsOn(shadowJar)
shadowJar.apply {
    archiveName = "users-api.jar"
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT
        )

        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
    targetCompatibility = JavaVersion.VERSION_13
}
