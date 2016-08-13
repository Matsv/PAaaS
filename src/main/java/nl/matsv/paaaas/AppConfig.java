package nl.matsv.paaaas;

import nl.matsv.paaaas.storage.StorageManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public StorageManager storageManager() {
        return new StorageManager();
    }
}
