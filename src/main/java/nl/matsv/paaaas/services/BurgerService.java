/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaaas.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.data.burger.BurgerOutput;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Service
public class BurgerService {
    private final String BURGER_URL = "https://github.com/mcdevs/Burger.git";
    private final String JAWA_URL = "https://github.com/TkTech/Jawa.git";
    private File BURGER_DIR = new File("Burger/");
    private File JAWA_DIR = new File("Jawa/");

    @Autowired
    private Gson gson;
    @Autowired
    private JythonService jythonService;

    @PostConstruct
    public void checkForUpdate() throws GitAPIException, IOException {
        if (!BURGER_DIR.isDirectory())
            BURGER_DIR.mkdir();
        if (!JAWA_DIR.isDirectory())
            JAWA_DIR.mkdir();

        if (hasMainFile()) {
            Git.open(BURGER_DIR).pull().call();
        } else {
            System.out.println("Starting to clone Burger");
            Git.cloneRepository()
                    .setURI(BURGER_URL)
                    .setDirectory(BURGER_DIR)
                    .call();
            System.out.println("Finished cloning Burger");
        }
        if (hasJawaMainFile()) {
            Git.open(JAWA_DIR).pull().call();
        } else {
            System.out.println("Starting to clone Jawa");
            Git.cloneRepository()
                    .setURI(JAWA_URL)
                    .setDirectory(JAWA_DIR)
                    .call();
            System.out.println("Finished cloning Jawa");
        }
    }

    public boolean hasMainFile() {
        return new File(BURGER_DIR, "munch.py").exists();
    }

    public boolean hasJawaMainFile() {
        return new File(JAWA_DIR, "setup.py").exists();
    }

    private boolean removeTempFile() {
        File f = getTempFile();
        if(f.exists()){
            try {
                FileUtils.forceDelete(f);
            } catch (IOException e) {
                f.deleteOnExit();
                System.out.println("Scheduling " + f.getAbsolutePath() + " to be deleted on exit.");
            }
        }
        return false;
    }

    private File getTempFile() {
        return new File(BURGER_DIR, "temp.json");
    }

    public boolean runBurger(File file, VersionDataFile versionDataFile) throws InterruptedException, IOException {
        removeTempFile();
        System.out.println("Start Burger for version " + versionDataFile.getVersion().getId());
        Throwable error = jythonService.execute(new File(BURGER_DIR, "munch.py"), new String[]{"--toppings", "packets,version,packetinstructions", "--output", getTempFile().getAbsolutePath(), file.getAbsolutePath()}, new File[]{JAWA_DIR});
        if (error != null) {
            System.out.println("Failed to run Burger...");
            error.printStackTrace();
            removeTempFile();
            return false;
        } else {
            FileReader fileWriter = new FileReader(getTempFile());
            JsonArray array = (JsonArray) new JsonParser().parse(fileWriter);
            BurgerOutput output = gson.fromJson(array.get(0).getAsJsonObject(), BurgerOutput.class);

            versionDataFile.setBurgerData(output);
            versionDataFile.getMetadata().setBurger(true);
            fileWriter.close();
            removeTempFile();
            return true;
        }
    }
}
