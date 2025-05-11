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

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CompileWasmRuntimeTask : JavaExec() {
    @get:InputFile
    abstract val closureCompiler: RegularFileProperty

    @get:InputFile
    abstract val externs: RegularFileProperty

    @get:InputFile
    abstract val eagRuntimeUtil: RegularFileProperty

    @get:InputFile
    abstract val eagRuntimeMain: RegularFileProperty

    @get:InputFile
    abstract val platformApplication: RegularFileProperty

    @get:InputFile
    abstract val platformAssets: RegularFileProperty

    @get:InputFile
    abstract val platformAudio: RegularFileProperty

    @get:InputFile
    abstract val platformFilesystem: RegularFileProperty

    @get:InputFile
    abstract val platformInput: RegularFileProperty

    @get:InputFile
    abstract val platformNetworking: RegularFileProperty

    @get:InputFile
    abstract val platformOpenGL: RegularFileProperty

    @get:InputFile
    abstract val platformRuntime: RegularFileProperty

    @get:InputFile
    abstract val platformScreenRecord: RegularFileProperty

    @get:InputFile
    abstract val platformVoiceClient: RegularFileProperty

    @get:InputFile
    abstract val platformWebRTC: RegularFileProperty

    @get:InputFile
    abstract val platformWebView: RegularFileProperty

    @get:InputFile
    abstract val clientPlatformSingleplayer: RegularFileProperty

    @get:InputFile
    abstract val serverPlatformSingleplayer: RegularFileProperty

    @get:InputFile
    abstract val wasmBufferAllocator: RegularFileProperty

    @get:InputFile
    abstract val webmDurationFix: RegularFileProperty

    @get:InputFile
    abstract val teavmRuntime: RegularFileProperty

    @get:InputFile
    abstract val eagEntryPoint: RegularFileProperty

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    override fun exec() {
        val closureCompilerArguments: List<String> = listOf(
            "--compilation_level", "ADVANCED_OPTIMIZATIONS",
            "--assume_function_wrapper",
            "--emit_use_strict",
            "--isolation_mode", "IIFE"
        )

        val sourceJavascriptFiles: List<String> = listOf(
            externs, eagRuntimeUtil, eagRuntimeMain,
            platformApplication, platformAssets, platformAudio, platformFilesystem,
            platformInput, platformNetworking, platformOpenGL, platformRuntime,
            platformScreenRecord, platformVoiceClient, platformWebRTC, platformWebView,
            clientPlatformSingleplayer, serverPlatformSingleplayer,
            wasmBufferAllocator, webmDurationFix, teavmRuntime, eagEntryPoint
        ).flatMap {
            listOf("--js", it.get().asFile.absolutePath)
        }

        val output: List<String> = listOf(
            "--js_output_file", output.get().asFile.absolutePath
        )

        classpath.plus(closureCompiler)
        args = closureCompilerArguments + sourceJavascriptFiles + output

        super.exec()
    }
}