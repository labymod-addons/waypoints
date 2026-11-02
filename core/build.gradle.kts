import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    addonMavenDependency("net.labymod.serverapi.integration:waypoints:1.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}

// The addon annotation processors only work on the main source set
tasks.compileTestJava {
    options.compilerArgs.add("-proc:none")
}

tasks.test {
    useJUnitPlatform()
}
