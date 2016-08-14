package nl.matsv.paaaas.module.modules;

import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.data.VersionMeta;
import nl.matsv.paaaas.module.Module;
import nl.matsv.paaaas.storage.StorageManager;
import nl.matsv.paaaas.services.BurgerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

public class BurgerModule extends Module {
    @Autowired
    private BurgerService burgerService;
    @Autowired
    private StorageManager storageManager;

    @Override
    public void run(VersionDataFile versionDataFile) {
        if (versionDataFile.getVersion().getReleaseTime().getTime() < 1400112000000L){
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
}
