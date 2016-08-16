/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaaas.tasks;

import com.google.gson.Gson;
import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.data.VersionMeta;
import nl.matsv.paaaas.data.minecraft.MinecraftData;
import nl.matsv.paaaas.data.minecraft.MinecraftVersion;
import nl.matsv.paaaas.module.ModuleLoader;
import nl.matsv.paaaas.storage.StorageManager;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
public class MinecraftTask {
    @Autowired
    private Gson gson;
    @Autowired
    private StorageManager storageManager;
    @Autowired
    private ModuleLoader moduleLoader;

    public void checkVersions() throws Exception {
        String json = IOUtils.toString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), StandardCharsets.UTF_8);
        MinecraftData mcData = gson.fromJson(json, MinecraftData.class);

        for (MinecraftVersion version : mcData.getVersions()) {
            if (!storageManager.hasVersion(version.getId())) {
                VersionDataFile vdf = new VersionDataFile(version, new VersionMeta(true, false, new ArrayList<>()), null, null);
                // Run Modules
                moduleLoader.runModules(vdf);
                // Save Data File!
                storageManager.saveVersion(vdf);
            }
        }
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60) // Run every minute
    public void versionTask() throws Exception {
        checkVersions();
    }
}
