package com.triangles.buildlogic

import com.android.build.api.dsl.ApplicationBuildType
import com.android.build.api.dsl.BuildType
import org.gradle.api.NamedDomainObjectContainer
import com.android.build.api.dsl.VariantDimension
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.util.Properties

fun NamedDomainObjectContainer<ApplicationBuildType>.staging(
    configure: ApplicationBuildType.() -> Unit,
) {
    maybeCreate("staging").apply(configure)
}

fun BuildType.buildConfigStringField(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
}

const val CONFIG_FILE_NAME = "Imoves-config.properties"


fun VariantDimension.buildConfigStringField(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
}

fun Project.loadConfigProperties(): Properties = Properties().apply {
    val configFile = rootProject.file(CONFIG_FILE_NAME)
    if (configFile.exists()) {
        configFile.inputStream().use { load(it) }
    }
}

fun Project.getConfigProperty(propertyName: String): String =
    loadConfigProperties().getProperty(propertyName)
        ?: throw GradleException("$CONFIG_FILE_NAME is missing property '$propertyName'.")
