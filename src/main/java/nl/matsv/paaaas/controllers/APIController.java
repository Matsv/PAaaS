/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaaas.controllers;

import com.google.gson.Gson;
import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.module.ModuleLoader;
import nl.matsv.paaaas.services.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/v1")
public class APIController {
    @Autowired
    private ModuleLoader moduleLoader;
    @Autowired
    private VersionService versionService;
    @Autowired
    private Gson gson;


    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public String compare(@RequestParam("old") String oldVersion, @RequestParam("new") String newVersion) {
        Optional<VersionDataFile> oldData = versionService.getVersion(oldVersion);
        Optional<VersionDataFile> newData = versionService.getVersion(newVersion);

        if (oldData.isPresent() && newData.isPresent() && oldData.get().getMetadata().isEnabled() && newData.get().getMetadata().isEnabled()){
            return gson.toJson(moduleLoader.compareModules(oldData.get(), newData.get()));
        } else {
            throw new IllegalArgumentException("One of the selected version doesn't exist or is not enabled.");
        }
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public VersionDataFile get(@RequestParam("id") String id) {
        Optional<VersionDataFile> data = versionService.getVersion(id);
        if (data.isPresent()) {
            return data.get();
        } else {
            throw new IllegalArgumentException("No version found with id: " + id);
        }

    }
}
