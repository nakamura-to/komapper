dependencies {
    api(project(":komapper-core"))
    implementation("org.postgresql:postgresql:42.2.19")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
}
