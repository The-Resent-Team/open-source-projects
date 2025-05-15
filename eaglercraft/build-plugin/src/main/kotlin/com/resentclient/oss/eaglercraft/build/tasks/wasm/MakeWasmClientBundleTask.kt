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

package com.resentclient.oss.eaglercraft.build.tasks.wasm

import net.lax1dude.eaglercraft.v1_8.buildtools.workspace.MakeWASMClientBundle
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class MakeWasmClientBundleTask : DefaultTask() {
    @get:InputFile
    abstract val epwSource: RegularFileProperty

    @get:InputFile
    abstract val epwMeta: RegularFileProperty

    @get:Optional
    @get:InputDirectory
    abstract val epwSearchDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val clientBundleOutputDir: DirectoryProperty

    @TaskAction
    fun makeClientBundle() {
        try {
            MakeWASMClientBundle.makeTheClient(
                arrayOf(
                    epwSource.get().asFile.absolutePath,
                    epwMeta.get().asFile.absolutePath,
                    clientBundleOutputDir.get().asFile.absolutePath
                ),
                if (epwSearchDirectory.isPresent) epwSearchDirectory.get().asFile else null
            )
        } catch (e: Exception) {
            throw GradleException(e.message ?: "Failed making wasm client bundle!", e)
        }
    }
}