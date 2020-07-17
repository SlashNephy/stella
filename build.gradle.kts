plugins {
    kotlin("multiplatform") version "1.3.72"
}

object ThirdpartyVersion {
    const val Ktor = "1.3.2"
    const val Penicillin = "5.0.0"
    const val KMongo = "4.0.3"
    const val KotlinLogging = "1.8.3"
    const val Logback = "1.2.3"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://dl.bintray.com/nephyproject/stable")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js {
        browser {
            webpackTask {
                sourceMaps = false
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))

                implementation("io.ktor:ktor-server-netty:${ThirdpartyVersion.Ktor}")
                implementation("io.ktor:ktor-locations:${ThirdpartyVersion.Ktor}")
                implementation("io.ktor:ktor-client-apache:${ThirdpartyVersion.Ktor}")

                implementation("blue.starry:penicillin:${ThirdpartyVersion.Penicillin}")
                implementation("org.litote.kmongo:kmongo-coroutine:${ThirdpartyVersion.KMongo}")
                implementation("org.jsoup:jsoup:1.11.3")
                implementation("org.apache.commons:commons-lang3:3.8.1")

                implementation("io.github.microutils:kotlin-logging:${ThirdpartyVersion.KotlinLogging}")
                implementation("ch.qos.logback:logback-classic:${ThirdpartyVersion.Logback}")
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
            }
        }

        named("jsMain") {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        named("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

task<JavaExec>("run") {
    dependsOn("build")

    group = "application"
    main = "io.ktor.server.netty.EngineMain"
    classpath(configurations["jvmRuntimeClasspath"], tasks["jvmJar"])
    args = listOf("-config=application.conf")
}
