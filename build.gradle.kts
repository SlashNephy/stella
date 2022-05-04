plugins {
    kotlin("multiplatform") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    id("com.github.johnrengelman.shadow") version "6.1.0"

    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("com.adarshr.test-logger") version "2.1.1"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
}

object Versions {
    const val Ktor = "1.5.2"
    const val Penicillin = "6.0.5"
    const val KMongo = "4.2.3"
    const val Jsoup = "1.13.1"

    const val JUnit = "5.7.0"

    const val KotlinLogging = "2.0.4"
    const val Logback = "1.2.3"
    const val Jansi = "1.18"
}

object Libraries {
    const val KtorServerCIO = "io.ktor:ktor-server-cio:${Versions.Ktor}"
    const val KtorLocations = "io.ktor:ktor-locations:${Versions.Ktor}"
    const val KtorSerialization = "io.ktor:ktor-serialization:${Versions.Ktor}"
    const val KtorClientCIO = "io.ktor:ktor-client-cio:${Versions.Ktor}"
    const val KtorClientLogging = "io.ktor:ktor-client-logging:${Versions.Ktor}"

    const val KMongoCoroutineSerialization = "org.litote.kmongo:kmongo-coroutine-serialization:${Versions.KMongo}"
    const val KMongoIdSerialization = "org.litote.kmongo:kmongo-id-serialization:${Versions.KMongo}"
    const val Penicillin = "blue.starry:penicillin:${Versions.Penicillin}"
    const val Jsoup = "org.jsoup:jsoup:${Versions.Jsoup}"

    const val KotlinLogging = "io.github.microutils:kotlin-logging:${Versions.KotlinLogging}"
    const val LogbackCore = "ch.qos.logback:logback-core:${Versions.Logback}"
    const val LogbackClassic = "ch.qos.logback:logback-classic:${Versions.Logback}"
    const val Jansi = "org.fusesource.jansi:jansi:${Versions.Jansi}"

    const val JUnitJupiter = "org.junit.jupiter:junit-jupiter:${Versions.JUnit}"

    const val KtorClientJs = "io.ktor:ktor-client-js:${Versions.Ktor}"

    val ExperimentalAnnotations = setOf(
        "kotlin.Experimental",
        "kotlin.ExperimentalStdlibApi",
        "kotlin.time.ExperimentalTime",
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "io.ktor.util.KtorExperimentalAPI",
        "io.ktor.locations.KtorExperimentalLocationsAPI",
        "kotlinx.coroutines.FlowPreview"
    )
}

repositories {
    mavenCentral()

    maven(url = "https://kotlin.bintray.com/kotlinx")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }
    js {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(Libraries.Penicillin)
                implementation(Libraries.KotlinLogging)
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
                implementation(Libraries.KtorServerCIO)
                implementation(Libraries.KtorLocations)
                implementation(Libraries.KtorSerialization)
                implementation(Libraries.KtorClientCIO)
                implementation(Libraries.KtorClientLogging)

                implementation(Libraries.KMongoCoroutineSerialization)
                implementation(Libraries.KMongoIdSerialization)
                implementation(Libraries.Jsoup)

                implementation(Libraries.LogbackCore)
                implementation(Libraries.LogbackClassic)
                implementation(Libraries.Jansi)
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
                implementation(Libraries.JUnitJupiter)
            }
        }

        named("jsMain") {
            dependencies {
                implementation(kotlin("stdlib-js"))

                implementation(Libraries.KtorClientJs)

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
                allWarningsAsErrors = true
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings.progressiveMode = true

        Libraries.ExperimentalAnnotations.forEach {
            languageSettings.useExperimentalAnnotation(it)
        }
    }
}

/*
 * Tests
 */

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    ignoreFailures.set(true)
}

buildtimetracker {
    reporters {
        register("summary") {
            options["ordered"] = "true"
            options["barstyle"] = "ascii"
            options["shortenTaskNames"] = "false"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
        events("passed", "failed")
    }

    testlogger {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
    }
}

// workaround for Kotlin/Multiplatform + Shadow issue
// Refer https://github.com/johnrengelman/shadow/issues/484#issuecomment-549137315.
task<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    group = "shadow"
    dependsOn("jvmJar")

    manifest {
        attributes("Main-Class" to "blue.starry.stella.AppKt")
    }
    archiveClassifier.set("all")

    val jvmMain = kotlin.jvm().compilations.getByName("main")
    from(jvmMain.output)
    configurations.add(jvmMain.compileDependencyFiles as Configuration)
}
