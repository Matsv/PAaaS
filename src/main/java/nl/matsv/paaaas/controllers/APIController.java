package nl.matsv.paaaas.controllers;

import nl.matsv.paaaas.storage.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1")
public class APIController {
    @Autowired
    StorageManager storageManager;

    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public void compare(@RequestParam("old") String oldVersion, @RequestParam("new") String newVersion) {
        throw new UnsupportedOperationException("Not implemented yet");
        // TODO return comparison json and require 2 args, old and new
    }
}
