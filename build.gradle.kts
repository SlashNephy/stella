plugins {
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
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
                implementation("blue.starry:penicillin:6.2.2")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
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
                implementation("io.ktor:ktor-server-cio:1.6.7")
                implementation("io.ktor:ktor-locations:1.6.7")
                implementation("io.ktor:ktor-serialization:1.6.7")

                implementation("io.ktor:ktor-client-cio:1.6.7")
                implementation("io.ktor:ktor-client-serialization:1.6.7")
                implementation("io.ktor:ktor-client-logging:1.6.7")

                implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.2.3")
                implementation("org.litote.kmongo:kmongo-id-serialization:4.2.3")
                implementation("org.jsoup:jsoup:1.14.3")
                implementation("com.squareup:gifencoder:0.10.1")

                implementation("ch.qos.logback:logback-classic:1.3.0-alpha12")
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

                implementation("io.ktor:ktor-client-js:1.6.7")

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
                apiVersion = "1.6"
                languageVersion = "1.6"
                allWarningsAsErrors = true
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings {
            progressiveMode = true

            optIn("kotlin.Experimental")
            optIn("kotlin.ExperimentalStdlibApi")
            optIn("kotlin.time.ExperimentalTime")
            optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            optIn("kotlinx.coroutines.DelicateCoroutinesApi")
            optIn("io.ktor.locations.KtorExperimentalLocationsAPI")
            optIn("kotlinx.coroutines.FlowPreview")
            optIn("io.ktor.util.InternalAPI")
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
