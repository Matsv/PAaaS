/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaas.module.modules.metadata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import nl.matsv.paaas.data.VersionDataFile;
import nl.matsv.paaas.data.VersionMeta;
import nl.matsv.paaas.data.metadata.MetadataEntry;
import nl.matsv.paaas.data.metadata.MetadataTree;
import nl.matsv.paaas.module.Module;
import nl.matsv.paaas.storage.StorageManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MetadataModule extends Module {
    @Autowired
    private StorageManager storageManager;
    @Autowired
    private Gson gson;
    private Map<String, ClassNode> classes = new HashMap<>();
    private String entity;
    private String dataWatcher;
    private String entityTypes;
    private String livingEntity;
    private String projectile;
    private String insentient;
    private String hanging;
    private String ambient;

    @Override
    public void run(VersionDataFile versionDataFile) {
        if (versionDataFile.getVersion().getReleaseTime().getTime() < 1292976000000L) {
            VersionMeta meta = versionDataFile.getMetadata();

            meta.setEnabled(false);
            meta.addError("This version is too old to Metadata.");

            System.out.println("Skip " + versionDataFile.getVersion().getId() + " for metadata because it's too old");
            return;
        }
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

        // Using magic technology find classes :D
        entity = findClassFromConstant("entityBaseTick");
        livingEntity = findClassFromConstant("livingEntityBaseTick");
        projectile = findClassFromConstant("ownerName", "inGround");
        insentient = findClassFromConstant("Unknown target reason, please report on the issue tracker");
        hanging = findClassFromConstant("Unknown target reason, please report on the issue tracker");

        entityTypes = findClassFromConstant("Skipping Entity with id {}", "Item");
        if (entityTypes == null) {
            // 1.9.4 & below
            entityTypes = findClassFromConstant("Skipping Entity with id ");
        }
        if (entity == null) {
            // b1.8.1 & below
            entity = findClassFromConstant("FallDistance"); // woo nbt
        }
        dataWatcher = findClassFromConstant("Data value id is too big with ");
        if (entity == null || entityTypes == null || dataWatcher == null) {
            VersionMeta meta = versionDataFile.getMetadata();

            meta.setEnabled(false);
            meta.addError("Metadata: Could not find constants, found " +
                    "Entity: " + entity + " Types: " + entityTypes + " Data Watcher: " + dataWatcher);

            System.out.println("Constants have changed!! " + versionDataFile.getVersion().getId());
            return;
        }

        List<String> queue = new ArrayList<>(classes.keySet());
        ClassTree object = new ClassTree("java.lang.Object");
        String current = null;
        while (queue.size() > 0) {
            if (object.contains(current) || current == null) {
                current = queue.get(0);
            }
            ClassNode clazz = classes.get(current);
            if (clazz != null) {
                // Check super
                String superC = clazz.superName.replace('/', '.');
                if (object.getName().equals(superC) || object.contains(superC)) {
                    object.insert(superC, current);
                    queue.remove(current);
                    current = null;
                } else {
                    if (queue.contains(superC)) {
                        current = superC;
                    } else {
                        queue.remove(current);
                        current = null;
                    }
                }
            } else {
                queue.remove(current);
                current = null;
            }
        }
        ClassTree tree = object.find(entity);
        MetadataTree output = metadataTree(tree, 0);
        versionDataFile.setMetadataTree(output);

        // Clean up classes
        classes.clear();
    }

    @Override
    public Optional<JsonObject> compare(VersionDataFile current, VersionDataFile other) {
        return Optional.of(gson.toJsonTree(current.getMetadataTree()).getAsJsonObject());
    }

    private Optional<String> resolveName(String clazz) {
        if (clazz.equals(entity)) return Optional.of("Entity");
        if (clazz.equals(livingEntity)) return Optional.of("LivingEntity");
        if (clazz.equals(projectile)) return Optional.of("Projectile");
        if (clazz.equals(insentient)) return Optional.of("Insentient");
        if (clazz.equals(hanging)) return Optional.of("Projectile");

        ClassNode entityTypesNode = classes.get(entityTypes);
        InvokeClassStringExtractor extractor = new InvokeClassStringExtractor(clazz, entityTypes);
        entityTypesNode.accept(extractor);
        if (extractor.getFoundName() == null)
            return Optional.empty();
        return Optional.of(extractor.getFoundName());
    }

    private MetadataTree metadataTree(ClassTree tree, int i) {
        MetadataTree output = new MetadataTree();

        List<MetadataEntry> mt = metadata(tree.getName());
        output.setClassName(tree.getName());
        output.setEntityName(resolveName(tree.getName()).orElse(""));

        for (MetadataEntry meta : mt) {
            meta.setIndex(i++);
        }
        output.getMetadata().addAll(mt);
        for (ClassTree item : tree.getChildren()) {
            output.getChildren().add(metadataTree(item, i));
        }
        return output;
    }

    private List<MetadataEntry> metadata(String name) {
        List<MetadataEntry> results = new ArrayList<>();
        ClassNode node = classes.get(name);
        List<MethodNode> methods = node.methods;
        for (MethodNode method : methods) {
            if (method.name.equals("<clinit>")) {
                // Static init
                MethodInsnNode lastMethod = null;
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof MethodInsnNode) {
                        if (((MethodInsnNode) insn).owner.equals(dataWatcher)) {
                            lastMethod = (MethodInsnNode) insn;
                        }
                    }
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (insn.getOpcode() == Opcodes.PUTSTATIC) {
                            if (lastMethod != null) {
                                if (!fieldInsn.owner.equals(name)) {
                                    continue;
                                }
                                // Find field
                                for (FieldNode fieldNode : (List<FieldNode>) node.fields) {
                                    if (fieldNode.name.equals(fieldInsn.name)) {
                                        // Got signature
                                        results.add(new MetadataEntry(0, fieldNode.name, lastMethod.name, fieldNode.signature));
                                        break;
                                    }
                                }
                            }
                        }
                        lastMethod = null;
                    }
                }
            }
        }
        return results;
    }

    private String findClassFromConstant(String... str) {
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
                            if (toFind.size() == 0)
                                return s.getKey();
                        }
                    }
                }
            }
        }
        return null;
    }
}
