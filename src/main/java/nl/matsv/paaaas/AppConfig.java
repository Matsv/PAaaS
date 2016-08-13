package nl.matsv.paaaas;

import com.google.gson.Gson;
import nl.matsv.paaaas.module.ModuleLoader;
import nl.matsv.paaaas.storage.StorageManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public StorageManager storageManager() {
        return new StorageManager();
    }

    @Bean
    public ModuleLoader moduleLoader() {
        return new ModuleLoader();
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}
