plugins {
    kotlin("jvm") version "1.9.20"
    `java-library`
}

group = "ru.misterpotz"
version = "1.1.0"

repositories {
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
