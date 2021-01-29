import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("multiplatform") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"

    // For testing
    id("com.adarshr.test-logger") version "2.1.1"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
}

object ThirdpartyVersion {
    const val Ktor = "1.5.1"
    const val Penicillin = "6.0.1"
    const val JsonKt = "6.0.0"
    const val KMongo = "4.2.3"
    const val Jsoup = "1.13.1"
    const val CommonsLang = "3.8.1"

    // For testing
    const val JUnit = "5.7.0"

    // For logging
    const val KotlinLogging = "2.0.4"
    const val Logback = "1.2.3"
    const val jansi = "1.18"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://dl.bintray.com/starry-blue-sky/stable")
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

                implementation("blue.starry:penicillin:${ThirdpartyVersion.Penicillin}")
                implementation("blue.starry:jsonkt:${ThirdpartyVersion.JsonKt}")
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
                implementation(kotlin("reflect"))

                implementation("io.ktor:ktor-server-cio:${ThirdpartyVersion.Ktor}")
                implementation("io.ktor:ktor-locations:${ThirdpartyVersion.Ktor}")
                implementation("io.ktor:ktor-client-cio:${ThirdpartyVersion.Ktor}")

                implementation("org.litote.kmongo:kmongo-coroutine:${ThirdpartyVersion.KMongo}")
                implementation("org.apache.commons:commons-lang3:${ThirdpartyVersion.CommonsLang}")

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
            useExperimentalAnnotation("io.ktor.locations.KtorExperimentalLocationsAPI")
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

task<JavaExec>("run") {
    dependsOn("build")

    group = "application"
    main = "blue.starry.stella.AppKt"
    classpath(configurations["jvmRuntimeClasspath"], tasks["jvmJar"])
}

// workaround for Kotlin/Multiplatform + Shadow issue
// Refer https://github.com/johnrengelman/shadow/issues/484#issuecomment-549137315.
task<ShadowJar>("shadowJar") {
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
