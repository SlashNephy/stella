plugins {
    kotlin("multiplatform") version "1.4.20"
}

object ThirdpartyVersion {
    const val Ktor = "1.4.3"
    const val Penicillin = "5.0.0"
    const val JsonKt = "5.0.0"
    const val KMongo = "4.2.2"
    const val KotlinLogging = "2.0.3"
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
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))

                implementation("blue.starry:jsonkt-common:${ThirdpartyVersion.JsonKt}")
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

            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            languageSettings.useExperimentalAnnotation("io.ktor.util.KtorExperimentalAPI")
            languageSettings.useExperimentalAnnotation("io.ktor.locations.KtorExperimentalLocationsAPI")
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

                implementation("io.ktor:ktor-client-js:${ThirdpartyVersion.Ktor}")
                implementation("blue.starry:jsonkt-js:${ThirdpartyVersion.JsonKt}")

                implementation(npm("bootstrap.native", "3.0.9"))
                implementation(npm("twemoji", "13.0.1"))
                implementation(npm("infinite-scroll", "3.0.6"))
                implementation(npm("js-cookie", "2"))
                implementation(npm("quicklink", "0.1.2"))
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
