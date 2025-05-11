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

import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildSuiteWASMExtension
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory

open class EaglercraftBuildSuiteWasmExtensionImpl(
    objects: ObjectFactory
) : EaglercraftBuildSuiteWASMExtension {
    override val closureCompiler: RegularFileProperty = objects.fileProperty()
    override val externs: RegularFileProperty = objects.fileProperty()
    override val eagRuntimeUtil: RegularFileProperty = objects.fileProperty()
    override val eagRuntimeMain: RegularFileProperty = objects.fileProperty()
    override val platformApplication: RegularFileProperty = objects.fileProperty()
    override val platformAssets: RegularFileProperty = objects.fileProperty()
    override val platformAudio: RegularFileProperty = objects.fileProperty()
    override val platformFilesystem: RegularFileProperty = objects.fileProperty()
    override val platformInput: RegularFileProperty = objects.fileProperty()
    override val platformNetworking: RegularFileProperty = objects.fileProperty()
    override val platformOpenGL: RegularFileProperty = objects.fileProperty()
    override val platformRuntime: RegularFileProperty = objects.fileProperty()
    override val platformScreenRecord: RegularFileProperty = objects.fileProperty()
    override val platformVoiceClient: RegularFileProperty = objects.fileProperty()
    override val platformWebRTC: RegularFileProperty = objects.fileProperty()
    override val platformWebView: RegularFileProperty = objects.fileProperty()
    override val clientPlatformSingleplayer: RegularFileProperty = objects.fileProperty()
    override val serverPlatformSingleplayer: RegularFileProperty = objects.fileProperty()
    override val wasmBufferAllocator: RegularFileProperty = objects.fileProperty()
    override val webmDurationFix: RegularFileProperty = objects.fileProperty()
    override val teavmRuntime: RegularFileProperty = objects.fileProperty()
    override val eagEntryPoint: RegularFileProperty = objects.fileProperty()
    override val runtimeOutput: RegularFileProperty = objects.fileProperty()

    override val epwSource: RegularFileProperty = objects.fileProperty()
    override val epwMeta: RegularFileProperty = objects.fileProperty()
    override val clientBundleOutput: DirectoryProperty = objects.directoryProperty()
}