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
package com.resentclient.oss.eaglercraft.build.tasks

import net.lax1dude.eaglercraft.v1_8.buildtools.workspace.MakeSignedClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Lax1dude has expressed his intentions to remove support for signed clients
 */
@Deprecated(message = "To be removed in a future eaglercraft update", level = DeprecationLevel.WARNING)
abstract class MakeSignedClientTask : DefaultTask() {
    @get:InputFile
    abstract val signedBundleTemplate: RegularFileProperty

    @get:InputFile
    abstract val javascriptSource: RegularFileProperty

    @get:InputFile
    abstract val assets: RegularFileProperty

    @get:Optional
    @get:InputFile
    abstract val languageMetadata: RegularFileProperty

    @get:InputFile
    abstract val signedClientTemplate: RegularFileProperty

    @get:InputFile
    abstract val updateDownloadSources: RegularFileProperty

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun makeSignedClient() {
        try {
            val params: MutableList<String> = ArrayList()

            // signed bundle
            params.add(signedBundleTemplate.get().asFile.absolutePath)
            // js
            params.add(javascriptSource.get().asFile.absolutePath)
            // assets
            params.add(assets.get().asFile.absolutePath)
            // lang (optional)
            if (languageMetadata.isPresent) params.add(languageMetadata.get().asFile.absolutePath)
            // signed client
            params.add(signedClientTemplate.get().asFile.absolutePath)
            // update sources
            params.add(updateDownloadSources.get().asFile.absolutePath)
            // output
            params.add(output.get().asFile.absolutePath)

            MakeSignedClient.main(params.toTypedArray())
        } catch (e: Exception) {
            throw GradleException(e.message!!, e)
        }
    }
}