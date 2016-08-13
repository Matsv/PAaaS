package nl.matsv.paaaas.modules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ModuleLoader {
    private static List<Class<? extends Module>> modules = new ArrayList<>();

    static {
        // Add modules
        modules.add(BurgerModule.class);
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
}
