plugins {
    `java-gradle-plugin`
    `maven-publish`

    id("java")
    kotlin("jvm")
}

extra["projectGroup"] = project.properties["resent.oss.eaglecraft.build.project.group"]
extra["projectVersion"] = project.properties["resent.oss.eaglecraft.build.project.version"]
extra["artifactId"] = "plugin"

group = extra["projectGroup"] as String
version = extra["projectVersion"] as String

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.jcraft:jzlib:1.1.3")
    implementation("org.tukaani:xz:1.10")
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

gradlePlugin {
    plugins {
        create("eaglercraftBuildPlugin") {
            id = "com.resentclient.oss.eaglercraft.build"
            implementationClass = "com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildPlugin"
        }
    }

    testSourceSets(
        sourceSets["test"]
    )
}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            artifactId = extra["artifactId"].toString()
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}