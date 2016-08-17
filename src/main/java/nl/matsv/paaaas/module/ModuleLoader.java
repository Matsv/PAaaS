/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaaas.module;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.module.modules.BurgerModule;
import nl.matsv.paaaas.module.modules.JarModule;
import nl.matsv.paaaas.module.modules.metadata.MetadataModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModuleLoader {
    private static List<Class<? extends Module>> modules = new ArrayList<>();
    @Autowired
    private Gson gson;

    static {
        // Add module
        modules.add(JarModule.class);
        modules.add(BurgerModule.class);
        modules.add(MetadataModule.class);
    }

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    public <T extends Module> T initModule(Class<T> clazz) {
        try {
            T m = clazz.newInstance();
            beanFactory.autowireBean(m);
            return m;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize module: " + clazz.getName(), e);
        }
    }

    public void runModules(VersionDataFile vdf) {
        for (Class<? extends Module> module : modules) {
            initModule(module).run(vdf);
        }
    }

    public JsonObject compareModules(VersionDataFile oldV, VersionDataFile newV){
        JsonObject obj = new JsonObject();

        obj.add("oldVersion", new JsonObject());
        obj.add("newVersion", new JsonObject());

        JsonObject oldObject = obj.get("oldVersion").getAsJsonObject();
        JsonObject newObject = obj.get("newVersion").getAsJsonObject();

        for (Class<? extends Module> module : modules) {
            Optional<JsonObject> oldOutput = initModule(module).compare(oldV, newV);
            Optional<JsonObject> newOutput = initModule(module).compare(newV, oldV);

            if (oldOutput.isPresent() && newOutput.isPresent()) {
                oldObject.add(module.getSimpleName(), oldOutput.get());
                newObject.add(module.getSimpleName(), newOutput.get());
            }
        }
        return obj;
    }
}
