plugins {
    kotlin("multiplatform") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }

    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation("blue.starry:penicillin:6.2.3")
                implementation("io.github.microutils:kotlin-logging:2.1.23")

                implementation("io.ktor:ktor-server-cio:1.6.8")
                implementation("io.ktor:ktor-locations:1.6.8")
                implementation("io.ktor:ktor-serialization:1.6.8")

                implementation("io.ktor:ktor-client-cio:1.6.8")
                implementation("io.ktor:ktor-client-serialization:1.6.8")
                implementation("io.ktor:ktor-client-logging:1.6.8")

                implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.4.0")
                implementation("org.litote.kmongo:kmongo-id-serialization:4.4.0")
                implementation("org.jsoup:jsoup:1.15.2")
                implementation("com.squareup:gifencoder:0.10.1")

                implementation("ch.qos.logback:logback-classic:1.2.11")
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions {
                apiVersion = "1.6"
                languageVersion = "1.6"
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings {
            progressiveMode = true

            optIn("kotlin.RequiresOptIn")
        }
    }
}

// workaround for Kotlin/Multiplatform + Shadow issue
// Refer https://github.com/johnrengelman/shadow/issues/484#issuecomment-549137315.
task<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    group = "shadow"
    dependsOn("jvmJar")

    manifest {
        attributes("Main-Class" to "blue.starry.stella.MainKt")
    }
    archiveClassifier.set("all")

    val jvmMain = kotlin.jvm().compilations.getByName("main")
    from(jvmMain.output)
    configurations.add(jvmMain.runtimeDependencyFiles)
}
