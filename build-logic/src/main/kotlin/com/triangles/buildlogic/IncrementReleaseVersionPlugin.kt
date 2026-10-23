package com.triangles.buildlogic

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.Properties

/**
 * Auto-bumps the release version in Dinar-config.properties: VER-NUMBER
 * (versionCode) by 1, and VERSION-NAME's last dot-segment (versionName's
 * patch number) by 1 — but only when a release build was actually requested
 * on the command line (e.g. `assembleRelease`, `assembleGOOGLERelease`,
 * `bundleRelease`), never for a plain `build`/`assemble`, or for
 * debug/staging-only invocations, and never merely from Gradle configuring
 * the project (e.g. `./gradlew tasks`).
 *
 * This deliberately does the bump eagerly at configuration time rather than
 * via a task: AGP validates the release variant's versionCode/versionName/
 * outputFileName properties before any task runs, so a value that only
 * becomes available once some *other* task has executed is treated as
 * "never configured" and fails validation. Reading, writing and handing AGP
 * a plain value up front sidesteps that entirely.
 */
class IncrementReleaseVersionPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val isBuildingRelease = gradle.startParameter.taskNames.any { requested ->
            requested.substringAfterLast(":").contains("release", ignoreCase = true)
        }
        if (!isBuildingRelease) return@with

        val configFile = rootProject.file("Dinar-config.properties")
        val (bumpedVersionCode, bumpedVersionName) = bumpReleaseVersion(configFile)

        val androidComponents = extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            if (variant.buildType != "release") return@onVariants

            variant.outputs.forEach { output ->
                output.versionCode.set(bumpedVersionCode)
                output.versionName.set(bumpedVersionName)
            }
        }
    }

    private fun bumpReleaseVersion(file: File): Pair<Int, String> {
        val properties = Properties().apply { file.inputStream().use { load(it) } }

        val currentVersionCode = properties.getProperty("VER-NUMBER")?.toIntOrNull()
            ?: throw GradleException("Dinar-config.properties is missing a numeric 'VER-NUMBER'.")
        val currentVersionName = properties.getProperty("VERSION-NAME")
            ?: throw GradleException("Dinar-config.properties is missing 'VERSION-NAME'.")

        val bumpedVersionCode = currentVersionCode + 1
        val bumpedVersionName = bumpPatch(currentVersionName)

        replaceProperty(file, "VER-NUMBER", bumpedVersionCode.toString())
        replaceProperty(file, "VERSION-NAME", bumpedVersionName)

        println(
            "Release version bumped: VER-NUMBER $currentVersionCode -> $bumpedVersionCode, " +
                "VERSION-NAME $currentVersionName -> $bumpedVersionName"
        )

        return bumpedVersionCode to bumpedVersionName
    }

    private fun bumpPatch(versionName: String): String {
        val segments = versionName.split(".").toMutableList()
        val patch = segments.last().toIntOrNull()
            ?: throw GradleException("VERSION-NAME '$versionName' does not end in a numeric patch segment.")
        segments[segments.lastIndex] = (patch + 1).toString()
        return segments.joinToString(".")
    }

    private fun replaceProperty(file: File, key: String, newValue: String) {
        val original = file.readText()
        // git's autocrlf can make this file CRLF on checkout. Splitting/joining
        // on whichever separator it actually uses (rather than readLines(),
        // which drops a trailing blank entry and normalizes line endings)
        // preserves both the line-ending style and trailing-newline-or-not,
        // so this doesn't introduce an unrelated diff on every bump.
        val lineSeparator = if (original.contains("\r\n")) "\r\n" else "\n"

        var replaced = false
        val updated = original.split(lineSeparator).map { line ->
            if (!replaced && line.startsWith("$key=")) {
                replaced = true
                "$key=$newValue"
            } else {
                line
            }
        }
        if (!replaced) throw GradleException("Could not find a '$key=' line in ${file.name}.")
        file.writeText(updated.joinToString(lineSeparator))
    }
}
