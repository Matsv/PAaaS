/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaaas.services;

import org.python.core.PyString;
import org.python.core.PySystemState;
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
        Throwable exception = null;
        try {
            interpreter.execfile(pythonScript.getAbsolutePath());
        } catch (RuntimeException e) {
            exception = e;
        }
        return exception;
    }
}
