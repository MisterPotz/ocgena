plugins {
    id("kotlin")
    //kotlin("kapt") // fallback to this if ksp goes crazy
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
    kotlin("plugin.serialization") version "1.9.20"
}

group = "ru.misterpotz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    //implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation(project(":ocgena-math-parexper"))
    implementation("com.charleskorn.kaml:kaml:0.55.0")

    implementation("com.google.dagger:dagger:2.48")
    //kapt("com.google.dagger:dagger-compiler:2.48.1") // fallback to this if ksp goes crazy
    ksp("com.google.dagger:dagger-compiler:2.48")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

kotlin {
    jvmToolchain(8)
}

tasks.test {
    useJUnitPlatform()
}
