import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
    kotlin("jvm") version "1.9.20"
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

application {
    mainClass.set("ru.misterpotz.application.ServerKt")
}
