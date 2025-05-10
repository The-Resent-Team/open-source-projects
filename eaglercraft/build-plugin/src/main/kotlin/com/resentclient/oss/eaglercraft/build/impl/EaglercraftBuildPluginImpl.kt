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

import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildExtension
import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildPlugin
import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildSuiteJSExtension
import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildTarget
import com.resentclient.oss.eaglercraft.build.tasks.CompileEpkTask
import com.resentclient.oss.eaglercraft.build.tasks.MakeOfflineDownloadTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class EaglercraftBuildPluginImpl : EaglercraftBuildPlugin {
    override fun apply(project: Project) {
        val ext: EaglercraftBuildExtension = project.extensions.create(
            "eaglercraftBuild",
            EaglercraftBuildExtensionImpl::class.java,
        )

        project.afterEvaluate {
            ext.getSuites().forEach { suite ->
                when (suite.target.get()) {
                    EaglercraftBuildTarget.JAVASCRIPT -> {
                        val jsConfig: EaglercraftBuildSuiteJSExtension = suite.getJs()
                        val capitalizedName: String = suite.name.get().replaceFirstChar { it.uppercase() }

                        val compileEpkTaskName: String = "compile${capitalizedName}Epk"
                        val makeOfflineDownloadTaskName: String = "make${capitalizedName}OfflineDownload"

                        val compileEpkTask: TaskProvider<CompileEpkTask> =
                            project.tasks.register(compileEpkTaskName, CompileEpkTask::class.java) { task ->
                                task.sources.convention(suite.epkSources)
                                task.output.convention(suite.epkOutput)
                            }

                        val makeOfflineDownloadTask: TaskProvider<MakeOfflineDownloadTask> =
                            project.tasks.register(makeOfflineDownloadTaskName, MakeOfflineDownloadTask::class.java) { task ->
                                task.dependsOn(suite.sourceGeneratorTaskName.get())
                                task.dependsOn(compileEpkTask)

                                task.offlineDownloadTemplate.convention(jsConfig.offlineDownloadTemplate)
                                task.javascriptSource.convention(suite.sourceGeneratorOutput)
                                task.eaglerAssets.convention(compileEpkTask.get().output)

                                // don't set main output & international output

                                task.languageMetadata.convention(jsConfig.languageMetadata)
                            }
                    }
                    EaglercraftBuildTarget.WASM_GC -> TODO()
                }
            }
        }
    }
}