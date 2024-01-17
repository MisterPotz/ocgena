plugins {
    kotlin("jvm") version "1.9.20"
    //kotlin("kapt") // fallback to this if ksp goes crazy
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
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
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation(kotlin("reflect"))
    testImplementation("io.mockk:mockk:1.13.8")

}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}
//
//compileKotlin {
//    kotlinOptions.jvmTarget = '11'
//}
//
//compileTestKotlin {
//    kotlinOptions.jvmTarget = '11'
//}