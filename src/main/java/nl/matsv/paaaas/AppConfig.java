package nl.matsv.paaaas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    private final AutowireCapableBeanFactory beanFactory;

    @Autowired
    public AppConfig(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public DataProvider dataProvider() {
        DataProvider provider = new DataProvider();
        beanFactory.autowireBean(provider);
        return provider;
    }
}
