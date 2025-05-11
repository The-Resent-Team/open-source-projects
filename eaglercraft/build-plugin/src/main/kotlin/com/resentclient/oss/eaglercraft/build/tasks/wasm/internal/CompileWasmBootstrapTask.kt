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

package com.resentclient.oss.eaglercraft.build.tasks.wasm.internal

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
abstract class CompileWasmBootstrapTask : JavaExec() {
    @get:InputFile
    abstract val closureCompiler: RegularFileProperty

    @get:InputFile
    abstract val externs: RegularFileProperty

    @get:InputFile
    abstract val main: RegularFileProperty

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
            externs, main
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