import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
    kotlin("multiplatform") version "1.8.0"
    application
}

group = "ru.misterpotz"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        nodejs {
            testTask {
                this.enabled = true
            }
        }
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    // Enable source maps for debugging
                    webpackConfig.sourceMaps = true
                    webpackConfig.cssSupport {
                        this.enabled.set(false)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("guru.nidi:graphviz-kotlin:0.18.1")

                implementation("io.ktor:ktor-server-netty:2.0.2")
                implementation("io.ktor:ktor-server-html-builder-jvm:2.0.2")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.8.1")
                implementation("guru.nidi:graphviz-kotlin:0.18.1")

            }

        }
        val jsMain by getting {
            dependencies {
                implementation(npm("peggy", "3.0.1"))
                val pathToLocalNpmModule = file("./ocdot-parser/") //rootProject.projectDir.resolve("ocgena-js/")

                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.8.0")
                println("path to the module $pathToLocalNpmModule")
                implementation(npm("ocdot-parser", pathToLocalNpmModule))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))

                val pathToLocalNpmModule = file("./ocdot-parser/") //rootProject.projectDir.resolve("ocgena-js/")

                println("path to the module $pathToLocalNpmModule")
                implementation(npm("ocdot-parser", pathToLocalNpmModule))

                implementation(kotlin("test-js"))

            }

        }
    }
}

application {
    mainClass.set("ru.misterpotz.application.ServerKt")
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}

//tasks.named("jsProductionExecutableCompileSync") {
//    dependsOn(tasks.named("jsNodeDevelopmentRun"))
//}


//tasks.register<Delete>("deleteJsBinaries") {
//    val dir = layout.projectDirectory.dir("ocgenajs").asFile
//    if (dir.exists()) {
//        delete(layout.projectDirectory.dir("ocgenajs"))
//    }
//    doLast {
//        project.mkdir("ocgenajs")
//    }
//}
//
//
//tasks.register<Copy>("exportJsBinaries") {
//    dependsOn("compileProductionExecutableKotlinJs")
//    dependsOn("deleteJsBinaries")
//
//    from(layout.buildDirectory.dir("js/node_modules/ocgena/"))
//    into(layout.projectDirectory.dir("ocgenajs"))
//    mustRunAfter("compileProductionExecutableKotlinJs")
//}


tasks.register<Delete>("deleteJsBinaries") {
    val dir = layout.projectDirectory.dir("ocgenajs").asFile

    val kotlinJs = layout.projectDirectory.dir("ocgenajs/kotlin").asFile
    val nodeModules = layout.projectDirectory.dir("ocgenajs/node_modules").asFile

    if (kotlinJs.exists()) {
        delete(kotlinJs)
    }
    if (nodeModules.exists()) {
        delete(nodeModules)
    }
    val packageJson = layout.projectDirectory.file("ocgenajs/package.json").asFile
    if (packageJson.exists()) {
        delete(packageJson)
    }

    doLast {
        if (!dir.exists()) {
            project.mkdir("ocgenajs")
        }
    }
}


tasks.register<Copy>("exportJsBinaries") {
    dependsOn("compileProductionExecutableKotlinJs")
    dependsOn("deleteJsBinaries")

    from(layout.buildDirectory.dir("js/node_modules/ocgena/"))
    into(layout.projectDirectory.dir("ocgenajs"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mustRunAfter("compileProductionExecutableKotlinJs")

}

