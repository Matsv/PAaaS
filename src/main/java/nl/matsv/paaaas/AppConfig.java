package nl.matsv.paaaas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public DataProvider dataProvider() {
        return new DataProvider();
    }
}
