/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaas.module.modules;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.matsv.paaas.data.VersionDataFile;
import nl.matsv.paaas.data.VersionMeta;
import nl.matsv.paaas.data.burger.BurgerOutput;
import nl.matsv.paaas.data.burger.BurgerPacket;
import nl.matsv.paaas.data.burger.BurgerPackets;
import nl.matsv.paaas.data.wiki.WikiData;
import nl.matsv.paaas.module.Module;
import nl.matsv.paaas.services.BurgerService;
import nl.matsv.paaas.services.WikiService;
import nl.matsv.paaas.storage.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BurgerModule extends Module {
    @Autowired
    private BurgerService burgerService;
    @Autowired
    private StorageManager storageManager;
    @Autowired
    private Gson gson;
    @Autowired
    private WikiService wikiService;

    @Override
    public void run(VersionDataFile versionDataFile) {
        if (versionDataFile.getVersion().getReleaseTime().getTime() < 1400112000000L) {
            VersionMeta meta = versionDataFile.getMetadata();

            meta.setEnabled(false);
            meta.addError("This version is too old to Burger.");

            System.out.println("Skip " + versionDataFile.getVersion().getId() + " for burger because it's too old");
            return;
        }

        File jar = new File(storageManager.getJarDirectory(), versionDataFile.getVersion().getId() + ".jar");
        if (!jar.exists()) {
            System.out.println("Cannot start Burger because the jar file doesn't exists " + jar.getName());
            return;
        }

        if (!burgerService.hasMainFile()) {
            System.out.println("Cannot start Burger because the munch.py file doesn't exists ");
            return;
        }

        try {
            boolean success = burgerService.runBurger(jar, versionDataFile);
            if (success)
                System.out.println("Burger finished successfully for version " + versionDataFile.getVersion().getId());
            else
                System.out.println("Burger didn't finished successfully for version " + versionDataFile.getVersion().getId());
        } catch (Exception e) {
            System.out.println("Generating Burger for version " + versionDataFile.getVersion().getId() + " failed");
            versionDataFile.getMetadata().addError(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Optional<JsonElement> compare(VersionDataFile current, VersionDataFile other) {
        if (current.getBurgerData() == null)
            return Optional.empty();

        BurgerOutput bout = current.getBurgerData();
        JsonObject output = new JsonObject();

        Optional<String> protocolId = Optional.ofNullable(bout.getVersion().get("protocol"));

        output.add("protocol", gson.toJsonTree(
                protocolId.isPresent() ?
                        protocolId.get() : "Not provided by Burger")
        );

        Map<String, BurgerPacket> changedPackets = new HashMap<>();

        bout.getPackets().getPacket().entrySet().stream()
                .forEach(entry -> changedPackets.put(entry.getKey(), entry.getValue()));

        output.add("changedPackets", gson.toJsonTree(changedPackets));

        Set<String> states = bout.getPackets().getStates().keySet();
        output.add("states", gson.toJsonTree(states.toArray()));

        Set<String> directions = bout.getPackets().getDirections().keySet();
        output.add("directions", gson.toJsonTree(directions.toArray()));

        try {
            if (protocolId.isPresent()) {
                int pid = Integer.parseInt(protocolId.get());

                Optional<WikiData> data = wikiService.getWikiData(pid);

                if (data.isPresent())
                    output.add("wiki_data", gson.toJsonTree(data.get()));
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return Optional.of(output);
    }
}
