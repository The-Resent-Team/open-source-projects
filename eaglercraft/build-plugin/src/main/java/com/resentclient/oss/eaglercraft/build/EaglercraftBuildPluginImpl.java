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

package com.resentclient.oss.eaglercraft.build;

import com.resentclient.oss.eaglercraft.build.api.EaglercraftBuildPlugin;
import com.resentclient.oss.eaglercraft.build.tasks.CompileEpkTask;
import com.resentclient.oss.eaglercraft.build.tasks.MakeOfflineDownloadTask;
import com.resentclient.oss.eaglercraft.build.tasks.MakeSignedClientTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class EaglercraftBuildPluginImpl implements EaglercraftBuildPlugin {
    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        EaglercraftBuildPlugin.super.apply(project);
    }

    @SuppressWarnings("removal")
    @Override
    public void registerJavascriptSuite(String name, String javascriptGeneratorTaskName) {
        String nameCapitalized = name.substring(0, 1).toUpperCase() + name.substring(1);

        String compileEpkTaskName = "compile" + nameCapitalized + "Epk";
        String makeOfflineDownloadTaskName = "make" + nameCapitalized + "OfflineDownload";
        String makeSignedClientTaskName = "make" + nameCapitalized + "SignedClient";

        TaskProvider<CompileEpkTask> compileEpkTask = this.project.getTasks().register(compileEpkTaskName, CompileEpkTask.class);

        this.project.getTasks().register(makeOfflineDownloadTaskName, MakeOfflineDownloadTask.class, (task) -> {
            task.dependsOn(compileEpkTask);
            task.dependsOn(javascriptGeneratorTaskName);
        });

        this.project.getTasks().register(makeSignedClientTaskName, MakeSignedClientTask.class, (task) -> {
            task.dependsOn(compileEpkTask);
            task.dependsOn(javascriptGeneratorTaskName);
        });
    }
}
