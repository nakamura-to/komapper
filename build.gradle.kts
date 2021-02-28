import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.4.31" apply false
    id("tanvd.kosogor") version "1.0.10"
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "tanvd.kosogor")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("0.34.2")
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
