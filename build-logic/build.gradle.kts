import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    `kotlin-dsl`
}

group = "com.triangles.buildlogic"

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    compileOnly(libs.findLibrary("android-gradle-plugin").get())
    compileOnly(libs.findLibrary("compose-gradle-plugin").get())
    compileOnly(libs.findLibrary("ksp-gradle-plugin").get())
}

gradlePlugin {
    plugins {
        register("helpers") {
            id = "imoves.helpers"
            implementationClass = "com.triangles.buildlogic.HelpersPlugin"
        }
    }
}
