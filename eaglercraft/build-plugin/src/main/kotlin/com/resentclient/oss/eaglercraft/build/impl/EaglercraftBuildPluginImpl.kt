/*
 * Eaglercraft Build Plugin is a gradle plugin built to simplify and modularize tasks when building Eaglercraft.
 * Copyright (C) 2025 cire3
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.resentclient.oss.eaglercraft.build.impl

import com.resentclient.oss.eaglercraft.build.api.*
import com.resentclient.oss.eaglercraft.build.tasks.common.CompileEpkTask
import com.resentclient.oss.eaglercraft.build.tasks.js.MakeOfflineDownloadTask
import com.resentclient.oss.eaglercraft.build.tasks.wasm.CompileWasmRuntimeTask
import com.resentclient.oss.eaglercraft.build.tasks.wasm.MakeWasmClientBundleTask
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

open class EaglercraftBuildPluginImpl : Plugin<Project> {
    override fun apply(project: Project) {
        val ext: EaglercraftBuildExtension = project.extensions.create(
            "eaglercraftBuild",
            EaglercraftBuildExtensionImpl::class.java,
        )

        project.afterEvaluate {
            ext.getSuites().forEach { suite ->
                when (suite.target.get()) {
                    EaglercraftBuildTarget.JAVASCRIPT -> {
                        registerJsSuite(project, suite)
                    }
                    EaglercraftBuildTarget.WASM_GC -> {
                        registerWasmSuite(project, suite)
                    }
                    null -> {
                        throw GradleException("Unknown eaglercraft build target ${suite.target.get()}")
                    }
                }
            }
        }
    }
}

private fun registerJsSuite(project: Project, suite: EaglercraftBuildSuiteExtension) {
    val jsConfig: EaglercraftBuildSuiteJSExtension = suite.getJs()
    val capitalizedName: String = suite.name.get().replaceFirstChar { it.uppercase() }

    val compileEpkTaskName: String = "compile${capitalizedName}Epk"
    val compileLanguageEpkTaskName: String = "compile${capitalizedName}LanguageEpk"
    val assembleComponentsTaskName: String = "assemble${capitalizedName}Components"
    val makeOfflineDownloadTaskName: String = "make${capitalizedName}OfflineDownload"

    val compileEpkTask: TaskProvider<CompileEpkTask> =
        project.tasks.register(compileEpkTaskName, CompileEpkTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.epkSources.convention(suite.epkSources)
            task.epkOutput.convention(suite.epkOutput)
            task.epkCompression.convention(EaglercraftBuildEpkCompression.GZIP)
        }

    val compileLanguageEpkTask: TaskProvider<CompileEpkTask> =
        project.tasks.register(compileLanguageEpkTaskName, CompileEpkTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.epkSources.convention(suite.languageMetadataInput)
            task.epkOutput.convention(suite.languageEpkOutput)
            task.epkCompression.convention(EaglercraftBuildEpkCompression.GZIP)
        }

    val assembleComponentsTask: TaskProvider<DefaultTask> =
        project.tasks.register(assembleComponentsTaskName, DefaultTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.dependsOn(compileEpkTask)
            task.dependsOn(compileLanguageEpkTask)
            task.dependsOn(suite.sourceGeneratorTaskName)
        }

    val makeOfflineDownloadTask: TaskProvider<MakeOfflineDownloadTask> =
        project.tasks.register(makeOfflineDownloadTaskName, MakeOfflineDownloadTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.dependsOn(assembleComponentsTask)

            task.offlineDownloadTemplate.convention(jsConfig.offlineDownloadTemplate)
            task.javascriptSource.convention(jsConfig.sourceGeneratorOutput)
            task.eaglerAssets.convention(suite.epkOutput)

            task.mainOutput.convention(jsConfig.mainOutput)
            task.internationalOutput.convention(jsConfig.internationalOutput)

            task.languageMetadata.convention(suite.languageEpkOutput.asFile.get().absolutePath)
        }
}

private fun registerWasmSuite(project: Project, suite: EaglercraftBuildSuiteExtension) {
    val wasmConfig: EaglercraftBuildSuiteWASMExtension = suite.getWasm()
    val capitalizedName: String = suite.name.get().replaceFirstChar { it.uppercase() }

    val compileEpkTaskName: String = "compile${capitalizedName}Epk"
    val compileLanguageEpkTaskName: String = "compile${capitalizedName}LanguageEpk"
    val compileEagRuntimeTaskName: String = "compile${capitalizedName}WasmRuntime"
    val assembleComponentsTaskName: String = "assemble${capitalizedName}Components"
    val makeWasmClientBundleTaskName: String = "make${capitalizedName}WasmClientBundle"

    val compileEpkTask: TaskProvider<CompileEpkTask> =
        project.tasks.register(compileEpkTaskName, CompileEpkTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.epkSources.convention(suite.epkSources)
            task.epkOutput.convention(suite.epkOutput)
            task.epkCompression.convention(EaglercraftBuildEpkCompression.NONE)
        }

    val compileLanguageEpkTask: TaskProvider<CompileEpkTask> =
        project.tasks.register(compileLanguageEpkTaskName, CompileEpkTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.epkSources.convention(suite.languageMetadataInput)
            task.epkOutput.convention(suite.languageEpkOutput)
            task.epkCompression.convention(EaglercraftBuildEpkCompression.NONE)
        }

    val compileWasmRuntimeTask: TaskProvider<CompileWasmRuntimeTask> =
        project.tasks.register(compileEagRuntimeTaskName, CompileWasmRuntimeTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.dependsOn(compileEpkTask)
            task.dependsOn(compileLanguageEpkTask)

            task.mainClass.convention(wasmConfig.closureMainClass)
            task.classpath += project.files(wasmConfig.closureCompiler)

            task.closureInputFiles.setFrom(wasmConfig.closureInputFiles)
            task.runtimeOutput.convention(wasmConfig.runtimeOutput)
        }

    val assembleComponentsTask: TaskProvider<DefaultTask> =
        project.tasks.register(assembleComponentsTaskName, DefaultTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.dependsOn(compileEpkTask)
            task.dependsOn(compileLanguageEpkTask)
            task.dependsOn(compileWasmRuntimeTask)
            task.dependsOn(suite.sourceGeneratorTaskName)
        }

    val makeClientTask: TaskProvider<MakeWasmClientBundleTask> =
        project.tasks.register(makeWasmClientBundleTaskName, MakeWasmClientBundleTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.dependsOn(assembleComponentsTask)

            task.epwSource.convention(wasmConfig.epwSource)
            task.epwMeta.convention(wasmConfig.epwMeta)
            task.epwSearchDirectory.convention(wasmConfig.epwSearchDirectory)
            task.clientBundleOutputDir.convention(wasmConfig.clientBundleOutputDir)
        }
}