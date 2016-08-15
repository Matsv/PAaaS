package nl.matsv.paaaas.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BurgerService {
    private final String BURGER_URL = "https://github.com/mcdevs/Burger.git";
    @Autowired
    private StorageManager storageManager;
    @Autowired
    private Gson gson;

    public void cloneBurger() throws GitAPIException {
        System.out.println("Starting to clone burger");
        Git.cloneRepository()
                .setURI(BURGER_URL)
                .setDirectory(new File("Burger/"))
                .call();
        System.out.println("Finished to clone burger");
    }

    @PostConstruct
    public void checkForUpdate() throws GitAPIException, IOException {
        if (hasMainFile())
            Git.open(new File("Burger/")).pull().call();
        else
            cloneBurger();
    }

    private void setup() {
        System.out.println("Running setup.py for Burger");
        // TODO run python setup.py install. with the default python package, this has to be executed as root.
    }

    public boolean hasMainFile() {
        return new File(storageManager.getBurgerDirectory(), "munch.py").exists();
    }

    private boolean removeTempFile() {
        File f = getTempFile();
        return f.exists() && f.delete();
    }

    private File getTempFile() {
        return new File(storageManager.getBurgerDirectory(), "temp.json");
    }

    public boolean runBurger(File file, VersionDataFile versionDataFile) throws InterruptedException, IOException {
        removeTempFile();
        System.out.println("Start Burger for version " + versionDataFile.getVersion().getId());

        Process process = Runtime.getRuntime().exec(String.format("python munch.py --toppings packets,version,packetinstructions --output temp.json %s", file.getAbsolutePath()), new String[0], storageManager.getBurgerDirectory());

        int exitCode = process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        try {
            if (exitCode != 0) {
                System.out.println("Something went wrong while trying to run Burger for version " + versionDataFile.getVersion().getId() + " exitCode: " + exitCode);

                List<String> errors = new ArrayList<>();
                while ((line = reader.readLine()) != null)
                    errors.add(line);

                versionDataFile.getMetadata().setEnabled(false);
                versionDataFile.getMetadata().addErrors(errors.toArray(new String[0]));

                return false;
            } else {
                FileReader fileWriter = new FileReader(getTempFile());
                JsonArray array = (JsonArray) new JsonParser().parse(fileWriter);
                BurgerOutput output = gson.fromJson(array.get(0).getAsJsonObject(), BurgerOutput.class);

                // Add states
                for (Map.Entry<String, BurgerPacket> entry : output.getPackets().getPacket().entrySet()) {
                    String state = entry.getKey().split("_")[0]; // get state from string until Burger adds it
                    entry.getValue().setState(state);
                }

                versionDataFile.setBurgerData(output);
                versionDataFile.getMetadata().setBurger(true);
                return true;
            }
        } finally {
            reader.close();
        }
    }
}
