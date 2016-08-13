package nl.matsv.paaaas.controllers;

import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.data.minecraft.MinecraftData;
import nl.matsv.paaaas.data.minecraft.MinecraftLatest;
import nl.matsv.paaaas.data.minecraft.MinecraftVersion;
import nl.matsv.paaaas.storage.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api")
public class APIController {
    @Autowired
    StorageManager storageManager;

    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public MinecraftData compare() {
        return new MinecraftData(new MinecraftLatest("Hi", "bye"), storageManager.getMinecraftVersions());
    }
}
