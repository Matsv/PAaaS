package nl.matsv.paaaas.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.data.burger.BurgerOutput;
import nl.matsv.paaaas.data.burger.BurgerPacket;
import nl.matsv.paaaas.storage.StorageManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

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
        return f.exists() && f.delete();
    }

    private File getTempFile() {
        return new File(BURGER_DIR, "temp.json");
    }

    public boolean runBurger(File file, VersionDataFile versionDataFile) throws InterruptedException, IOException {
        removeTempFile();
        System.out.println("Start Burger for version " + versionDataFile.getVersion().getId());
        Throwable error = jythonService.execute(new File(BURGER_DIR, "munch.py"), new String[]{"--toppings", "packets,version,packetinstructions", "--output", new File(BURGER_DIR, "temp.json").getAbsolutePath(), file.getAbsolutePath()}, new File[]{JAWA_DIR});
        if (error != null) {
            System.out.println("Failed to run Burger...");
            error.printStackTrace();
            return false;
        } else {
            FileReader fileWriter = new FileReader(getTempFile());
            JsonArray array = (JsonArray) new JsonParser().parse(fileWriter);
            BurgerOutput output = gson.fromJson(array.get(0).getAsJsonObject(), BurgerOutput.class);

            versionDataFile.setBurgerData(output);
            versionDataFile.getMetadata().setBurger(true);
            return true;
        }
    }
}
