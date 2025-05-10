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

import com.resentclient.oss.eaglercraft.build.impl.EaglercraftBuildPluginImpl
import org.gradle.api.Plugin
import org.gradle.api.Project

interface EaglercraftBuildPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(EaglercraftBuildPluginImpl::class.java);
    }
}