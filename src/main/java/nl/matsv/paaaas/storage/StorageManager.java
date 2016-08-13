package nl.matsv.paaaas.storage;

import nl.matsv.paaaas.data.VersionDataFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StorageManager {
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
            return Optional.of(null); // TODO
        } else {
            return Optional.empty();
        }
    }

    public void saveVersion(String version, VersionDataFile dataFile) {
        File target = new File(dataDirectory, version + ".json");
        // TODO
    }

    public boolean hasVersion(String version) {
        File target = new File(dataDirectory, version + ".json");
        return target.exists();
    }

    public List<String> getVersions() {
        return Arrays.stream(dataDirectory.listFiles((dir, name) -> name.endsWith(".json")))
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
