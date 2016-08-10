package nl.matsv.paaaas.tasks;

import com.google.gson.Gson;
import nl.matsv.paaaas.data.MinecraftData;
import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class MinecraftTask {
    private final Gson gson = new Gson();

    @Async
    public void checkVersions() throws Exception {
        String s = IOUtils.toString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json"), StandardCharsets.UTF_8);
        System.out.println(gson.fromJson(s, MinecraftData.class));
    }

    @Scheduled(fixedDelay = 1000 * 60 * 10) // Run every 10 minutes?
    public void versionTask() throws Exception {
        checkVersions();
    }


}
