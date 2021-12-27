plugins {
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

object Versions {
    const val Ktor = "1.6.5"
    const val Penicillin = "6.2.1"
    const val KMongo = "4.2.3"
    const val Jsoup = "1.14.3"

    const val KotlinLogging = "2.0.11"
    const val Logback = "1.2.3"
}

object Libraries {
    const val KtorServerCIO = "io.ktor:ktor-server-cio:${Versions.Ktor}"
    const val KtorLocations = "io.ktor:ktor-locations:${Versions.Ktor}"
    const val KtorSerialization = "io.ktor:ktor-serialization:${Versions.Ktor}"
    const val KtorClientCIO = "io.ktor:ktor-client-cio:${Versions.Ktor}"
    const val KtorClientSerialization = "io.ktor:ktor-client-serialization:${Versions.Ktor}"
    const val KtorClientLogging = "io.ktor:ktor-client-logging:${Versions.Ktor}"

    const val KMongoCoroutineSerialization = "org.litote.kmongo:kmongo-coroutine-serialization:${Versions.KMongo}"
    const val KMongoIdSerialization = "org.litote.kmongo:kmongo-id-serialization:${Versions.KMongo}"
    const val Penicillin = "blue.starry:penicillin:${Versions.Penicillin}"
    const val Jsoup = "org.jsoup:jsoup:${Versions.Jsoup}"

    const val KotlinLogging = "io.github.microutils:kotlin-logging:${Versions.KotlinLogging}"
    const val LogbackCore = "ch.qos.logback:logback-core:${Versions.Logback}"
    const val LogbackClassic = "ch.qos.logback:logback-classic:${Versions.Logback}"

    const val KtorClientJs = "io.ktor:ktor-client-js:${Versions.Ktor}"

    val ExperimentalAnnotations = setOf(
        "kotlin.Experimental",
        "kotlin.ExperimentalStdlibApi",
        "kotlin.time.ExperimentalTime",
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlinx.coroutines.DelicateCoroutinesApi",
        "io.ktor.locations.KtorExperimentalLocationsAPI",
        "kotlinx.coroutines.FlowPreview",
        "io.ktor.util.InternalAPI"
    )
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
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
                implementation(Libraries.KtorClientSerialization)
                implementation(Libraries.KtorClientLogging)

                implementation(Libraries.KMongoCoroutineSerialization)
                implementation(Libraries.KMongoIdSerialization)
                implementation(Libraries.Jsoup)
                implementation("com.squareup:gifencoder:0.10.1")

                implementation(Libraries.LogbackCore)
                implementation(Libraries.LogbackClassic)
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
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
                apiVersion = "1.5"
                languageVersion = "1.5"
                allWarningsAsErrors = true
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings.progressiveMode = true

        Libraries.ExperimentalAnnotations.forEach {
            languageSettings.optIn(it)
        }
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
