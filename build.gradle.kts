plugins {
    `java-library`
    alias(libs.plugins.paperweight.userdev)
}

group = "net.okocraft.images"
version = "1.0"

val mcVersion = libs.versions.paper.get().replaceAfter(".build", "").removeSuffix(".build")
val fullVersion = "${version}-mc${mcVersion}"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand("projectVersion" to version)
        }
    }

    jar {
        archiveFileName = "Images-${fullVersion}.jar"
    }
}
