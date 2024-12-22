plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

group = "ktor-native"
version = "1.0.0"

val ktorVersion = "3.0.3"
val kotlinxSerializationVersion = "1.7.3"

repositories {
    mavenCentral()
}

kotlin {
    // targets
    linuxX64 {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
            }
        }
    }
    macosX64 {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
            }
        }
    }
    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Ktor HTTP Client for Kotlin Native
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                // Logging
                implementation("io.github.microutils:kotlin-logging:3.0.5")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation(kotlin("test"))
            }
        }
    }
}
