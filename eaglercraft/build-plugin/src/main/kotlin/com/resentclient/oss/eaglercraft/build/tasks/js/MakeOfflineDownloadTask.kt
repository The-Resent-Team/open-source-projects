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
package com.resentclient.oss.eaglercraft.build.tasks.js

import net.lax1dude.eaglercraft.v1_8.buildtools.workspace.MakeOfflineDownload
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class MakeOfflineDownloadTask : DefaultTask() {
    @get:InputFile
    abstract val offlineDownloadTemplate: RegularFileProperty

    @get:InputFile
    abstract val javascriptSource: RegularFileProperty

    @get:InputFile
    abstract val eaglerAssets: RegularFileProperty

    @get:OutputFile
    abstract val mainOutput: RegularFileProperty

    @get:Optional
    @get:OutputFile
    abstract val internationalOutput: RegularFileProperty

    @get:Optional
    @get:InputFile
    abstract val languageMetadata: RegularFileProperty

    @TaskAction
    fun makeOfflineDownload() {
        try {
            val params: MutableList<String> = ArrayList()

            // offline download
            params.add(offlineDownloadTemplate.get().asFile.absolutePath)
            // js source
            params.add(javascriptSource.get().asFile.absolutePath)
            // eagler assets
            params.add(eaglerAssets.get().asFile.absolutePath)
            // US output
            params.add(mainOutput.get().asFile.absolutePath)
            // international output (optional)
            if (internationalOutput.isPresent)
                params.add(internationalOutput.get().asFile.absolutePath)
            // langs (optional)
            if (languageMetadata.isPresent)
                params.add(languageMetadata.get().asFile.absolutePath)

            MakeOfflineDownload.main(params.toTypedArray())
        } catch (e: Exception) {
            throw GradleException(e.message?: "Failed creating offline download!", e)
        }
    }
}