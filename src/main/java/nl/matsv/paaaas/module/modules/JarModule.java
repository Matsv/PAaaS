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
}
