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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class EaglercraftBuildSuiteWasmExtensionImpl(
    objects: ObjectFactory
) : EaglercraftBuildSuiteWASMExtension {
    override val closureCompiler: RegularFileProperty = objects.fileProperty()
    override val closureMainClass: Property<String> = objects.property(String::class.java)
    override val closureInputFiles: ConfigurableFileCollection = objects.fileCollection()
    override val runtimeOutput: RegularFileProperty = objects.fileProperty()

    override val epwSource: RegularFileProperty = objects.fileProperty()
    override val epwMeta: RegularFileProperty = objects.fileProperty()
    override val epwSearchDirectory: DirectoryProperty = objects.directoryProperty()
    override val clientBundleOutputDir: DirectoryProperty = objects.directoryProperty()
}