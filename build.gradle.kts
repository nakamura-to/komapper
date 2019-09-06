import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.3.50" apply true
    id("tanvd.kosogor") version "1.0.4" apply true
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0" apply true
}

subprojects {
    apply(plugin = "tanvd.kosogor")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("0.34.2")
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
    }
}

repositories {
    mavenCentral()
    jcenter()
}
