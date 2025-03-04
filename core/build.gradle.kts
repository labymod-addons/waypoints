import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    addonMavenDependency("net.labymod.serverapi.integration:waypoints:1.0.2")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}