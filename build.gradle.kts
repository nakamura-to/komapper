plugins {
    kotlin("jvm") version "1.3.41" apply true
    id("tanvd.kosogor") version "1.0.4" apply true
}

subprojects {
    apply(plugin = "tanvd.kosogor")
}

repositories {
    mavenCentral()
    jcenter()
}
