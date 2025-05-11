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
    val makeOfflineDownloadTaskName: String = "make${capitalizedName}OfflineDownload"

    val compileEpkTask: TaskProvider<CompileEpkTask> =
        project.tasks.register(compileEpkTaskName, CompileEpkTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.epkSources.convention(suite.epkSources)
            task.epkOutput.convention(suite.epkOutput)
        }

    val makeOfflineDownloadTask: TaskProvider<MakeOfflineDownloadTask> =
        project.tasks.register(makeOfflineDownloadTaskName, MakeOfflineDownloadTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.dependsOn(suite.sourceGeneratorTaskName.get())
            task.dependsOn(compileEpkTask)

            task.offlineDownloadTemplate.convention(jsConfig.offlineDownloadTemplate)
            task.javascriptSource.convention(suite.sourceGeneratorOutput)
            task.eaglerAssets.convention(compileEpkTask.get().epkOutput)

            task.mainOutput.convention(jsConfig.mainOutput)
            task.internationalOutput.convention(jsConfig.internationalOutput)

            task.languageMetadata.convention(jsConfig.languageMetadata)
        }
}

private fun registerWasmSuite(project: Project, suite: EaglercraftBuildSuiteExtension) {
    val wasmConfig: EaglercraftBuildSuiteWASMExtension = suite.getWasm()
    val capitalizedName: String = suite.name.get().replaceFirstChar { it.uppercase() }

    val compileEpkTaskName: String = "compile${capitalizedName}Epk"
    val compileEagRuntimeTaskName: String = "compile${capitalizedName}WasmRuntime"
    val makeWasmClientBundleTaskName: String = "make${capitalizedName}WasmClientBundle"

    val compileEpkTask: TaskProvider<CompileEpkTask> =
        project.tasks.register(compileEpkTaskName, CompileEpkTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.epkSources.convention(suite.epkSources)
            task.epkOutput.convention(suite.epkOutput)
        }

    val compileWasmRuntimeTask: TaskProvider<CompileWasmRuntimeTask> =
        project.tasks.register(compileEagRuntimeTaskName, CompileWasmRuntimeTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.dependsOn(compileEpkTask)

            task.closureCompiler.convention(wasmConfig.closureCompiler)
            task.closureInputFiles.convention(wasmConfig.closureInputFiles)
            task.runtimeOutput.convention(wasmConfig.runtimeOutput)
        }

    val makeClientTask: TaskProvider<MakeWasmClientBundleTask> =
        project.tasks.register(makeWasmClientBundleTaskName, MakeWasmClientBundleTask::class.java) { task ->
            task.group = "eaglercraft build"

            task.dependsOn(compileEpkTask)
            task.dependsOn(compileWasmRuntimeTask)

            task.epwSource.convention(wasmConfig.epwSource)
            task.epwMeta.convention(wasmConfig.epwMeta)
            task.clientBundleOutput.convention(wasmConfig.clientBundleOutput)
        }
}