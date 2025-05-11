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

package com.resentclient.oss.eaglercraft.build.api

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.util.internal.ConfigureUtil

interface EaglercraftBuildSuiteExtension {
    val name: Property<String>
    val target: Property<EaglercraftBuildTarget>

    // EPK generation
    val epkSources: DirectoryProperty
    val epkOutput: RegularFileProperty

    // Source generation
    val sourceGeneratorTaskName: Property<String>
    val sourceGeneratorOutput: RegularFileProperty

    fun getJs(): EaglercraftBuildSuiteJSExtension

    fun js(action: Action<EaglercraftBuildSuiteJSExtension>): Unit

    fun js(closure: Closure<*>): Unit {
        js(ConfigureUtil.configureUsing(closure))
    }

    fun getWasm(): EaglercraftBuildSuiteWASMExtension

    fun wasm(action: Action<EaglercraftBuildSuiteWASMExtension>): Unit

    fun wasm(closure: Closure<*>): Unit {
        wasm(ConfigureUtil.configureUsing(closure))
    }
}