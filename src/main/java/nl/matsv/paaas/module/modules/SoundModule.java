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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.matsv.paaas.data.VersionDataFile;
import nl.matsv.paaas.data.VersionMeta;
import nl.matsv.paaas.storage.StorageManager;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SoundModule extends AbstractClassModule {
    @Autowired
    private Gson gson;
    @Autowired
    private StorageManager storageManager;

    @Override
    public void run(VersionDataFile versionDataFile) {
        if (versionDataFile.getVersion().getReleaseTime().getTime() < 1292976000000L) {
            VersionMeta meta = versionDataFile.getMetadata();

            meta.setEnabled(false);
            meta.addError("This version is too old to Sounds.");

            System.out.println("Skip " + versionDataFile.getVersion().getId() + " for metadata because it's too old");
            return;
        }
        // Load classes
        loadClasses(storageManager, versionDataFile);
        // Find sound file
        String soundClass = findClassFromConstant("Accessed Sounds before Bootstrap!");
        ClassNode node = classes.get(soundClass);
        List<String> sounds = new ArrayList<>();
        for (MethodNode method : (List<MethodNode>) node.methods) {
            if (method.name.equals("<clinit>")) {
                method.accept(new MethodVisitor(Opcodes.ASM5) {
                    private Object ldc;

                    @Override
                    public void visitLdcInsn(Object cst) {
                        ldc = cst;
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        if (opcode == Opcodes.INVOKESTATIC && ldc instanceof String && owner.equals(soundClass)) {
                            Type[] args = Type.getArgumentTypes(desc);
                            if (args.length == 1) {
                                if (args[0].getClassName().equals("java.lang.String")) {
                                    sounds.add((String) ldc);
                                }
                            }
                        }
                        ldc = null;
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }
                });
            }
        }
        versionDataFile.setSounds(sounds);
        // Clean up classes
        classes.clear();
    }

    @Override
    public Optional<JsonElement> compare(VersionDataFile current, VersionDataFile other) {
        return Optional.of(gson.toJsonTree(current.getSounds()));
    }
}
