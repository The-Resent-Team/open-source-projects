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

import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildSuiteJSExtension
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

class EaglercraftBuildSuiteJSExtensionImpl @Inject constructor(
    objects: ObjectFactory
) : EaglercraftBuildSuiteJSExtension {
    override val offlineDownloadTemplate: RegularFileProperty = objects.fileProperty()
    override val eaglerAssets: RegularFileProperty = objects.fileProperty()
    override val mainOutput: RegularFileProperty = objects.fileProperty()
    override val internationalOutput: RegularFileProperty = objects.fileProperty()
    override val languageMetadata: RegularFileProperty = objects.fileProperty()
}