import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.31"
}

group = "koma"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testRuntime("com.h2database:h2:1.4.199")
    testRuntime("org.junit.platform:junit-platform-launcher:1.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}