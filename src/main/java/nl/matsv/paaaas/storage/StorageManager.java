package nl.matsv.paaaas.storage;

import com.google.gson.Gson;
import lombok.Getter;
import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.data.minecraft.MinecraftVersion;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class StorageManager {
    @Autowired
    private Gson gson;
    private final File jarDirectory;
    private final File dataDirectory;

    public StorageManager() {
        jarDirectory = new File("jars/");
        if (!jarDirectory.exists()) {
            jarDirectory.mkdir();
        }

        dataDirectory = new File("data/");
        if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }
    }

    public Optional<VersionDataFile> getVersion(String version) {
        File target = new File(dataDirectory, version + ".json");
        if (target.exists()) {
            try (FileReader fileWriter = new FileReader(target)) {
                return Optional.of(gson.fromJson(fileWriter, VersionDataFile.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public void saveVersion(VersionDataFile dataFile) {
        File target = new File(dataDirectory, dataFile.getVersion().getId() + ".json");
        try (FileWriter fileWriter = new FileWriter(target)) {
            gson.toJson(dataFile, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasVersion(String version) {
        File target = new File(dataDirectory, version + ".json");
        return target.exists();
    }

    public List<String> getVersions() {
        return Arrays.stream(dataDirectory.listFiles((dir, name) -> name.endsWith(".json")))
                .map(File::getName).map((s) -> s.substring(0, s.length() - 5)) // Remove .json
                .collect(Collectors.toList());
    }

    public List<MinecraftVersion> getMinecraftVersions() {
        List<MinecraftVersion> versions = new ArrayList<>();
        for (String version : getVersions()) {
            Optional<VersionDataFile> vdf = getVersion(version);
            if (vdf.isPresent()) {
                versions.add(vdf.get().getVersion());
            }
        }
        return versions;
    }
}
