import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.8.21"
    application
}

group = "ru.misterpotz"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("ru.misterpotz.application.ServerKt")
}
