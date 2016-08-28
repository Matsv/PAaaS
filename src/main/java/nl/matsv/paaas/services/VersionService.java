/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaas.services;

import nl.matsv.paaas.data.VersionDataFile;
import nl.matsv.paaas.data.minecraft.MinecraftVersion;
import nl.matsv.paaas.storage.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class VersionService {
    private final StorageManager storageManager;
    private Map<String, VersionDataFile> versionMap = new ConcurrentHashMap<>();

    @Autowired
    public VersionService(StorageManager storageManager) {
        this.storageManager = storageManager;
        refreshVersions();
    }

    public void refreshVersions() {
        versionMap = new ConcurrentHashMap<>(storageManager.getVersionDataFiles());
    }

    public List<MinecraftVersion> getEnabledVersions() {
        return versionMap.values()
                .stream()
                .filter(file -> file.getMetadata().isEnabled())
                .map(VersionDataFile::getVersion)
                .sorted((o1, o2) -> {
                    if (o1.getReleaseTime() == null || o2.getReleaseTime() == null)
                        return 0;
                    return o2.getReleaseTime().compareTo(o1.getReleaseTime());
                })
                .collect(Collectors.toList());
    }

    public Optional<VersionDataFile> getVersion(String version) {
        if (!versionMap.containsKey(version))
            return Optional.empty();
        return Optional.of(versionMap.get(version));
    }
}
