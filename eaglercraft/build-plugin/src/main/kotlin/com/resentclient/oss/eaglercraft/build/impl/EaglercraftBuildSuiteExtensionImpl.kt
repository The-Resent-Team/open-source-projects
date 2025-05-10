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

import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildSuiteExtension
import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildSuiteJSExtension
import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildTarget
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

class EaglercraftBuildSuiteExtensionImpl
@Inject constructor(
    nameString: String,
    objects: ObjectFactory
) : EaglercraftBuildSuiteExtension {
    final override var name: Property<String> = objects.property(String::class.java)
    final override var target: Property<EaglercraftBuildTarget> = objects.property(EaglercraftBuildTarget::class.java)

    // epk things
    override var epkSources: DirectoryProperty = objects.directoryProperty()
    override var epkOutput: RegularFileProperty = objects.fileProperty()

    // source generators
    override var sourceGeneratorTaskName: Property<String> = objects.property(String::class.java)
    override var sourceGeneratorOutput: RegularFileProperty = objects.fileProperty()

    // js
    private var jsExtension: EaglercraftBuildSuiteJSExtension = objects.newInstance(EaglercraftBuildSuiteJSExtensionImpl::class.java)

    // wasm

    init {
        this.name.set(nameString)
    }

    override fun js(action: Action<EaglercraftBuildSuiteJSExtension>) {
        target.set(EaglercraftBuildTarget.JAVASCRIPT)

        action.execute(jsExtension)
    }
}