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

package com.resentclient.oss.eaglercraft.build.tasks;

import net.lax1dude.eaglercraft.v1_8.buildtools.workspace.MakeOfflineDownload;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

public abstract class MakeOfflineDownloadTask extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getOfflineDownloadTemplate();

    @InputFile
    public abstract RegularFileProperty getJavascriptSource();

    /**
     * .epw, .epk, etc
     * */
    @InputFile
    public abstract RegularFileProperty getEaglerAssets();

    @OutputFile
    public abstract RegularFileProperty getMainOutput();

    @OutputFile
    @Optional
    public abstract RegularFileProperty getInternationalOutput();

    @InputFile
    @Optional
    public abstract RegularFileProperty getLanguageMetadata();

    @TaskAction
    public void makeOfflineDownload() {
        try {
            List<String> params = new ArrayList<>();

            // offline download
            params.add(getOfflineDownloadTemplate().get().getAsFile().getAbsolutePath());
            // js source
            params.add(getJavascriptSource().get().getAsFile().getAbsolutePath());
            // eagler assets
            params.add(getEaglerAssets().get().getAsFile().getAbsolutePath());
            // US output
            params.add(getMainOutput().get().getAsFile().getAbsolutePath());
            // international output (optional)
            if (getInternationalOutput().isPresent())
                params.add(getInternationalOutput().get().getAsFile().getAbsolutePath());
            // langs (optional)
            if (getLanguageMetadata().isPresent())
                params.add(getLanguageMetadata().get().getAsFile().getAbsolutePath());

            MakeOfflineDownload.main(params.toArray(String[]::new));
        } catch (Exception e) {
            throw new GradleException(e.getMessage(), e);
        }
    }
}
