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
import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildSuiteWASMExtension
import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildTarget
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

class EaglercraftBuildSuiteExtensionImpl(
    nameString: String,
    objects: ObjectFactory
) : EaglercraftBuildSuiteExtension {
    override var name: Property<String> = objects.property(String::class.java)
    override var target: Property<EaglercraftBuildTarget> = objects.property(EaglercraftBuildTarget::class.java)

    // epk things
    override var epkSources: DirectoryProperty = objects.directoryProperty()
    override var epkOutput: RegularFileProperty = objects.fileProperty()

    // source generators
    override var sourceGeneratorTaskName: Property<String> = objects.property(String::class.java)

    // js
    private var jsExtension: EaglercraftBuildSuiteJSExtension = EaglercraftBuildSuiteJSExtensionImpl(objects)

    // wasm
    private var wasmExtension: EaglercraftBuildSuiteWASMExtension = EaglercraftBuildSuiteWasmExtensionImpl(objects)

    init {
        this.name.set(nameString)
    }

    override fun getJs(): EaglercraftBuildSuiteJSExtension {
        return jsExtension
    }

    override fun js(action: Action<EaglercraftBuildSuiteJSExtension>) {
        target.set(EaglercraftBuildTarget.JAVASCRIPT)
        target.disallowChanges()

        action.execute(jsExtension)
    }

    override fun getWasm(): EaglercraftBuildSuiteWASMExtension {
        return wasmExtension
    }

    override fun wasm(action: Action<EaglercraftBuildSuiteWASMExtension>) {
        target.set(EaglercraftBuildTarget.WASM_GC)
        target.disallowChanges()

        action.execute(wasmExtension)
    }
}