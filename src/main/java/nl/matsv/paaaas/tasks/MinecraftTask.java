package nl.matsv.paaaas.tasks;

import com.google.gson.Gson;
import nl.matsv.paaaas.data.minecraft.MinecraftData;
import nl.matsv.paaaas.data.minecraft.MinecraftVersion;
import nl.matsv.paaaas.storage.StorageManager;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class MinecraftTask {
    private final Gson gson = new Gson();
    @Autowired
    private StorageManager storageManager;

    @Async
    public void checkVersions() throws Exception {
        String json = IOUtils.toString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), StandardCharsets.UTF_8);
        MinecraftData mcData = gson.fromJson(json, MinecraftData.class);

        for (MinecraftVersion version : mcData.getVersions()) {
            if(!storageManager.hasVersion(version.getId())){
                // Download Jar
                // Run Modules
                // Save Data File!
            }
        }
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60 * 1) // Run every minute
    public void versionTask() throws Exception {
        checkVersions();
    }
}
