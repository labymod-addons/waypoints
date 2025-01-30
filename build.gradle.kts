plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "net.labymod.addons"
version = providers.environmentVariable("VERSION").getOrElse("0.0.1")

labyMod {
    defaultPackageName = "net.labymod.addons.waypoints"

    minecraft {
        registerVersion(versions.toTypedArray()) {

        }
    }

    addonInfo {
        namespace = "labyswaypoints"
        displayName = "Laby's Waypoints"
        author = "LabyMod"
        description = "Allows you to set waypoints in the world. Compatible with Laby's Minimap."
        minecraftVersion = "*"
        version = System.getenv().getOrDefault("VERSION", "0.0.1")
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    repositories {
        maven("https://libraries.minecraft.net/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}