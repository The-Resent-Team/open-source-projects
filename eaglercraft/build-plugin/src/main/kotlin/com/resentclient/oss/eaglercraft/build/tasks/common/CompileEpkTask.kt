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
package com.resentclient.oss.eaglercraft.build.tasks.common

import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildEpkCompression
import net.lax1dude.eaglercraft.v1_8.buildtools.workspace.CompilePackage
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.IOException

abstract class CompileEpkTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val epkSources: DirectoryProperty

    @get:OutputFile
    abstract val epkOutput: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val epkCompression: Property<EaglercraftBuildEpkCompression>

    @TaskAction
    fun compileEpk() {
        if (!epkCompression.isPresent)
            epkCompression.set(EaglercraftBuildEpkCompression.getDefault())

        try {
            CompilePackage.main(
                arrayOf(
                    epkSources.get().asFile.absolutePath,
                    epkOutput.get().asFile.absolutePath,
                    epkCompression.get().string
                )
            )
        } catch (e: IOException) {
            throw GradleException(e.message ?: "Failed compiling EPK!", e)
        }
    }
}