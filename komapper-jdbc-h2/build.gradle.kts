import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply true
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    api(project(":komapper-core"))
    implementation("com.h2database:h2:1.4.199")
    testRuntime("org.junit.platform:junit-platform-launcher:1.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
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
