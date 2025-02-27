import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    addonMavenDependency("net.labymod.serverapi.integration:waypoints:0.0.0")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}