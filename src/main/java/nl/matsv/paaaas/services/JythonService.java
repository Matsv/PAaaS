package nl.matsv.paaaas.services;

import org.python.core.*;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class JythonService {
    public Throwable execute(File pythonScript, String[] args, File[] classPath) {
        // Update environment variables, for storage.
        System.setProperty("python.home", new File("").getAbsolutePath());
        System.setProperty("python.executable", new File("").getAbsolutePath());

        PySystemState sys = new PySystemState();
        // Setup the args
        sys.argv.clear();
        sys.argv.append(new PyString(pythonScript.getName()));
        for (String arg : args) {
            sys.argv.append(new PyString(arg));
        }

        // Setup path
        sys.path.append(new PyString(pythonScript.getParentFile().getAbsolutePath()));
        for (File include : classPath) {
            sys.path.append(new PyString(include.getAbsolutePath()));
        }

        PythonInterpreter interpreter = new PythonInterpreter(null, sys);

        // Fix a few variables
        interpreter.set("__file__", new PyString(pythonScript.getAbsolutePath()));
        interpreter.set("__name__", new PyString("__main__"));
        // Run & Wait!
        Throwable[] exception = new Throwable[1];
        try {
            Thread t = new Thread() {
                public void run() {
                    try {
                        interpreter.execfile(pythonScript.getAbsolutePath());
                    } catch (Exception e) {
                        exception[0] = e;
                    }
                }
            };
            t.start();
            t.join();
        } catch (InterruptedException e) {
            exception[0] = e;
        }
        return exception[0];
    }
}
