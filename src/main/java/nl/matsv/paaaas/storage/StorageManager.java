/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaaas.storage;

import com.google.gson.Gson;
import lombok.Getter;
import nl.matsv.paaaas.data.VersionDataFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Component
public class StorageManager {
    private final File jarDirectory;
    private final File dataDirectory;
    @Autowired
    private Gson gson;

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

    public Map<String, VersionDataFile> getVersionDataFiles() {
        Map<String, VersionDataFile> map = new HashMap<>();
        for (String s : getVersions()) {
            Optional<VersionDataFile> op = getVersion(s);
            if (op.isPresent())
                map.put(s, op.get());
        }

        return map;
    }

}
