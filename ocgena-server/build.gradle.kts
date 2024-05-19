plugins {
    kotlin("jvm") version "1.9.20"
    id("java-library")
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    kotlin("plugin.serialization") version "1.9.20"

    id("io.ktor.plugin") version "2.3.8"
}

group = "ru.misterpotz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("ru.misterpotz.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

val exposedVersion: String by project
val ktorVersion: String by project
val kotlin_version: String by project
val logback_version: String by project

dependencies {
    api(project(":db_api"))
    api(project(":ocgena-domain"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("com.charleskorn.kaml:kaml:0.55.0")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.google.dagger:dagger:2.48")
    //kapt("com.google.dagger:dagger-compiler:2.48.1") // fallback to this if ksp goes crazy
    ksp("com.google.dagger:dagger-compiler:2.48")

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
//    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
}

tasks.test {
    useJUnitPlatform()
}