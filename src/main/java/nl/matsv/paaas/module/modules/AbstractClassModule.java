/*
 * Copyright (c) 2016 Mats & Myles
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaas.module.modules;

import nl.matsv.paaas.data.VersionDataFile;
import nl.matsv.paaas.module.Module;
import nl.matsv.paaas.storage.StorageManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class AbstractClassModule extends Module {
    protected Map<String, ClassNode> classes = new HashMap<>();

    public void loadClasses(StorageManager storageManager, VersionDataFile versionDataFile) {
        File file = new File(storageManager.getJarDirectory(), versionDataFile.getVersion().getId() + ".jar");

        // Generate Metadata
        JarFile jarFile;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            System.out.println("Missing jar file " + file.getAbsolutePath());
            return;
        }

        Enumeration<JarEntry> iter = jarFile.entries();
        while (iter.hasMoreElements()) {
            JarEntry entry = iter.nextElement();
            if (entry.getName().endsWith(".class") && (entry.getName().startsWith("net/minecraft") || !entry.getName().contains("/"))) {
                ClassReader reader;
                try {
                    reader = new ClassReader(jarFile.getInputStream(entry));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.EXPAND_FRAMES);
                classes.put(entry.getName().replace('/', '.').replace(".class", ""), node);
            }
        }
    }

    public String findClassFromConstant(String... str) {
        mainLoop:
        for (Map.Entry<String, ClassNode> s : classes.entrySet()) {
            List<String> toFind = new ArrayList<>(Arrays.asList(str));
            ClassNode clazz = s.getValue();
            List<MethodNode> methods = clazz.methods;
            for (MethodNode method : methods) {
                for (AbstractInsnNode insnNode : method.instructions.toArray()) {
                    if (insnNode instanceof LdcInsnNode) {
                        LdcInsnNode ldc = (LdcInsnNode) insnNode;
                        if (toFind.contains(ldc.cst)) {
                            toFind.remove(ldc.cst);
                            if (toFind.size() == 0) {
                                return s.getKey();
                            }
                        } else {
                            if (toFind.contains("-" + ldc.cst)) {
                                continue mainLoop;
                            }
                        }
                    }
                }
            }
            // Check if any negations
            for (String st : str) {
                if (st.startsWith("-")) {
                    toFind.remove(st);
                }
            }
            if (toFind.size() == 0) {
                return s.getKey();
            }
        }
        return null;
    }
}
