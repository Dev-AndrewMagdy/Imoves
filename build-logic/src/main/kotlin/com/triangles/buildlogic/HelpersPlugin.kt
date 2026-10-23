package com.triangles.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Puts this build-logic module's classes on the buildscript classpath of the
 * project that applies it. The plugin itself configures nothing; build scripts
 * still import the helpers they use, e.g.
 *
 * ```
 * import com.triangles.buildlogic.getConfigProperty
 * import com.triangles.buildlogic.buildConfigStringField
 * ```
 */
class HelpersPlugin : Plugin<Project> {
    override fun apply(target: Project) = Unit
}
