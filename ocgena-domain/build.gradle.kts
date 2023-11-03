plugins {
    id("kotlin")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.8.21"
}

group = "ru.misterpotz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation(project(":ocgena-math-parexper"))

    implementation("com.google.dagger:dagger:2.48.1")
    kapt("com.google.dagger:dagger-compiler:2.48.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
