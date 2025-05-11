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
 * MERCHANTABILITY or FITNESS FOR A P
 * ARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.resentclient.oss.eaglercraft.build.api

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

interface EaglercraftBuildSuiteWASMExtension {
    val closureCompiler: RegularFileProperty
    val externs: RegularFileProperty
    val eagRuntimeUtil: RegularFileProperty
    val eagRuntimeMain: RegularFileProperty
    val platformApplication: RegularFileProperty
    val platformAssets: RegularFileProperty
    val platformAudio: RegularFileProperty
    val platformFilesystem: RegularFileProperty
    val platformInput: RegularFileProperty
    val platformNetworking: RegularFileProperty
    val platformOpenGL: RegularFileProperty
    val platformRuntime: RegularFileProperty
    val platformScreenRecord: RegularFileProperty
    val platformVoiceClient: RegularFileProperty
    val platformWebRTC: RegularFileProperty
    val platformWebView: RegularFileProperty
    val clientPlatformSingleplayer: RegularFileProperty
    val serverPlatformSingleplayer: RegularFileProperty
    val wasmBufferAllocator: RegularFileProperty
    val webmDurationFix: RegularFileProperty
    val teavmRuntime: RegularFileProperty
    val eagEntryPoint: RegularFileProperty
    val runtimeOutput: RegularFileProperty

    val epwSource: RegularFileProperty
    val epwMeta: RegularFileProperty
    val clientBundleOutput: DirectoryProperty
}