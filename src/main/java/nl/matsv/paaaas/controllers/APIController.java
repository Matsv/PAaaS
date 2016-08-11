package nl.matsv.paaaas.controllers;

import nl.matsv.paaaas.data.minecraft.MinecraftData;
import nl.matsv.paaaas.data.minecraft.MinecraftLatest;
import nl.matsv.paaaas.data.minecraft.MinecraftVersion;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping(value = "/api")
public class APIController {

    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public MinecraftData compare() {
        return new MinecraftData(new MinecraftLatest("Hi", "bye"), Collections.singletonList(new MinecraftVersion("1", "snapshot", "nu", "net", "http://test.nl")));
    }
}
