/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaaas.module.modules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.module.Module;
import nl.matsv.paaaas.storage.StorageManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class JarModule extends Module {
    @Autowired
    private Gson gson;
    @Autowired
    private StorageManager storageManager;

    @Override
    public void run(VersionDataFile versionDataFile) {
        // Check if jar already exists
        File jar = new File(storageManager.getJarDirectory(), versionDataFile.getVersion().getId() + ".jar");
        if (jar.exists()) {
            return; // Ignore already downloaded
        }

        // Grab version info
        String json = null;
        try {
            json = IOUtils.toString(new URL(versionDataFile.getVersion().getUrl()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonObject data = gson.fromJson(json, JsonObject.class);
        String clientUrl = data.get("downloads").getAsJsonObject().get("client").getAsJsonObject().get("url").getAsString();
        // Download jar
        try {
            FileUtils.copyURLToFile(new URL(clientUrl), jar);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished downloading " + versionDataFile.getVersion().getId());
    }

    @Override
    public Optional<JsonObject> compare(VersionDataFile current, VersionDataFile other) {
        return Optional.of(gson.toJsonTree(current.getVersion()).getAsJsonObject());
    }
}
