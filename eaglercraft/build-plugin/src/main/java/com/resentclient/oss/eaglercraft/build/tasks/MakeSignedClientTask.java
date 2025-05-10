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

import net.lax1dude.eaglercraft.v1_8.buildtools.workspace.MakeSignedClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Lax1dude has expressed his intentions to remove support for signed clients
 * */
@Deprecated(forRemoval = true, since = "0.0.0")
public abstract class MakeSignedClientTask extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getSignedBundleTemplate();

    @InputFile
    public abstract RegularFileProperty getJavascriptSource();

    @InputFile
    public abstract RegularFileProperty getAssets();

    @InputFile
    @Optional
    public abstract RegularFileProperty getLanguageMetadata();

    @InputFile
    public abstract RegularFileProperty getSignedClientTemplate();

    @InputFile
    public abstract RegularFileProperty getUpdateDownloadSources();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @TaskAction
    public void makeSignedClient() {
        try {
            List<String> params = new ArrayList<>();

            // signed bundle
            params.add(getSignedBundleTemplate().get().getAsFile().getAbsolutePath());
            // js
            params.add(getJavascriptSource().get().getAsFile().getAbsolutePath());
            // assets
            params.add(getAssets().get().getAsFile().getAbsolutePath());
            // lang (optional)
            if (getLanguageMetadata().isPresent())
                params.add(getLanguageMetadata().get().getAsFile().getAbsolutePath());
            // signed client
            params.add(getSignedClientTemplate().get().getAsFile().getAbsolutePath());
            // update sources
            params.add(getUpdateDownloadSources().get().getAsFile().getAbsolutePath());
            // output
            params.add(getOutput().get().getAsFile().getAbsolutePath());

            MakeSignedClient.main(params.toArray(String[]::new));
        } catch (Exception e) {
            throw new GradleException(e.getMessage(), e);
        }
    }
}
