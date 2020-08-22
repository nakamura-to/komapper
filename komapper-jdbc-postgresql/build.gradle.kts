import tanvd.kosogor.proxy.publishJar

dependencies {
    api(project(":komapper-core"))
    implementation("org.postgresql:postgresql:42.2.6")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
}

publishJar {
    publication {
        artifactId = "komapper-jdbc-postgresql"
    }

    bintray {
        username = project.properties["bintrayUser"]?.toString() ?: System.getenv("BINTRAY_USER")
        secretKey = project.properties["bintrayApiKey"]?.toString() ?: System.getenv("BINTRAY_API_KEY")
        repository = "maven"
        info {
            githubRepo = "https://github.com/nakamura-to/komapper.git"
            vcsUrl = "https://github.com/nakamura-to/komapper.git"
            license = "Apache-2.0"
        }
    }
}
