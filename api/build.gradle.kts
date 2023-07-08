version = "0.1.0"

plugins {
    id("java-library")
}

dependencies {
    labyApi("api")

    // If you want to use external libraries, you can do that here.
    // The dependencies that are specified here are loaded into your project but will also
    // automatically be downloaded by labymod, but only if the repository is public.
    // If it is private, you have to add and compile the dependency manually.
    // You have to specify the repository, there are getters for maven central and sonatype, every
    // other repository has to be specified with their url. Example:
    // maven(mavenCentral(), "org.apache.httpcomponents:httpclient:4.5.13")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.INTERFACE
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

var publishToken = System.getenv("PUBLISH_TOKEN")

if (publishToken == null && project.hasProperty("net.labymod.distributor.publish-token")) {
    publishToken = project.property("net.labymod.distributor.publish-token").toString()
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            groupId = "net.labymod.addons"
            artifactId = "waypoints"
            version = rootProject.version.toString()

            from(components["java"])
        }
    }

    repositories {
        maven("https://dist.labymod.net/api/v1/maven/release/") {
            authentication {
                create<HttpHeaderAuthentication>("header")
            }

            credentials(HttpHeaderCredentials::class) {
                name = "Publish-Token"
                value = publishToken
            }
        }
    }
}
