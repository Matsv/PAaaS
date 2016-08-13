package nl.matsv.paaaas.module;

import nl.matsv.paaaas.data.VersionDataFile;
import nl.matsv.paaaas.module.modules.BurgerModule;
import nl.matsv.paaaas.module.modules.JarModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.ArrayList;
import java.util.List;

public class ModuleLoader {
    private static List<Class<? extends Module>> modules = new ArrayList<>();

    static {
        // Add module
        modules.add(JarModule.class);
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

    public void runModules(VersionDataFile vdf) {
        for (Class<? extends Module> module : modules) {
            initModule(module).run(vdf);
        }
    }
}
