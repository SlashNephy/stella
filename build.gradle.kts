import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
    kotlin("multiplatform") version "1.4.20"
    id("com.github.johnrengelman.shadow") version "6.0.0"

    // For testing
    id("com.adarshr.test-logger") version "2.0.0"
    id("build-time-tracker") version "0.11.1"
}

object ThirdpartyVersion {
    const val Ktor = "1.4.3"
    const val Penicillin = "5.0.0"
    const val JsonKt = "5.0.0"
    const val KMongo = "4.2.2"
    const val Jsoup = "1.13.1"

    // For testing
    const val JUnit = "5.7.0"

    // For logging
    const val KotlinLogging = "2.0.3"
    const val Logback = "1.2.3"
    const val jansi = "1.18"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/nephyproject/stable")
    maven(url = "https://dl.bintray.com/nephyproject/dev")
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
                implementation(kotlin("reflect"))

                implementation("io.ktor:ktor-server-netty:${ThirdpartyVersion.Ktor}")
                implementation("io.ktor:ktor-locations:${ThirdpartyVersion.Ktor}")
                implementation("io.ktor:ktor-client-cio:${ThirdpartyVersion.Ktor}")

                implementation("blue.starry:penicillin:${ThirdpartyVersion.Penicillin}")
                implementation("org.litote.kmongo:kmongo-coroutine:${ThirdpartyVersion.KMongo}")
                implementation("org.apache.commons:commons-lang3:3.8.1")

                // HTML parsing
                implementation("org.jsoup:jsoup:${ThirdpartyVersion.Jsoup}")

                // For logging
                implementation("io.github.microutils:kotlin-logging:${ThirdpartyVersion.KotlinLogging}")
                implementation("ch.qos.logback:logback-core:${ThirdpartyVersion.Logback}")
                implementation("ch.qos.logback:logback-classic:${ThirdpartyVersion.Logback}")
                implementation("org.fusesource.jansi:jansi:${ThirdpartyVersion.jansi}")
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter:${ThirdpartyVersion.JUnit}")
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

    targets.all {
        compilations.all {
            kotlinOptions {
                apiVersion = "1.4"
                languageVersion = "1.4"
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings.progressiveMode = true
        languageSettings.apply {
            useExperimentalAnnotation("kotlin.Experimental")
            useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
            useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            useExperimentalAnnotation("io.ktor.util.KtorExperimentalAPI")
        }
    }
}

/*
 * Tests
 */

buildtimetracker {
    reporters {
        register("summary") {
            options["ordered"] = "true"
            options["barstyle"] = "ascii"
            options["shortenTaskNames"] = "false"
        }
    }
}

testlogger {
    theme = ThemeType.MOCHA
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.shadowJar {
    manifest {
        attributes("Main-Class" to "blue.starry.stella.AppKt")
    }
}

task<JavaExec>("run") {
    dependsOn("build")

    group = "application"
    main = "blue.starry.stella.AppKt"
    classpath(configurations["jvmRuntimeClasspath"], tasks["jvmJar"])
}
