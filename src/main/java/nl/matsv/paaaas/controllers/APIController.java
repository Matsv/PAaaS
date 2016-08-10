package nl.matsv.paaaas.controllers;

import nl.matsv.paaaas.data.MinecraftData;
import nl.matsv.paaaas.data.MinecraftLatest;
import nl.matsv.paaaas.data.MinecraftVersion;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping(value = "/api")
public class APIController {

    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public MinecraftData hi() {
        return new MinecraftData(new MinecraftLatest("Hi", "bye"), Collections.singletonList(new MinecraftVersion("1", "snapshot", "nu", "net", "http://test.nl")));
    }
}
