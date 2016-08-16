/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaaas.module.modules.metadata;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.Stack;

class InvokeClassStringExtractor extends ClassVisitor {
    private final String entityTypes;
    private final String classToFind;
    public String foundName;

    public InvokeClassStringExtractor(String classToFind, String entityTypes) {
        super(Opcodes.ASM5);
        this.entityTypes = entityTypes;
        this.classToFind = classToFind;
    }

    public String getFoundName() {
        return foundName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        if (Modifier.isStatic(access) && foundName == null) {
            return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, methodName, desc, signature, exceptions)) {
                private Stack args = new Stack();

                @Override
                public void visitLdcInsn(Object cst) {
                    if (foundName != null) return;
                    args.push(cst);
                    super.visitLdcInsn(cst);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    if (foundName != null) return;
                    if (owner.equals(entityTypes)) {
                        // Take the last variables on the stack
                        Type[] argTypes = Type.getArgumentTypes(desc);
                        boolean clazz = false;
                        boolean string = false;
                        for (Type t : argTypes) {
                            if (t.getClassName().equals("java.lang.Class")) {
                                clazz = true;
                            }
                            if (t.getClassName().equals("java.lang.String")) {
                                string = true;
                            }
                        }
                        if (clazz && string) {
                            // Get last string
                            String className = null;
                            String entityName = null;
                            for (int i = 0; i < argTypes.length; i++) {
                                if (args.size() > 0) {
                                    Object pop = args.pop();
                                    if (pop instanceof Type) {
                                        if (!((Type) pop).getClassName().equals(classToFind)) {
                                            break;
                                        } else {
                                            if (entityName != null) {
                                                foundName = entityName;
                                                return;
                                            } else {
                                                className = ((Type) pop).getClassName();
                                            }
                                        }
                                    }
                                    if (pop instanceof String) {
                                        if (className != null) {
                                            foundName = (String) pop;
                                            return;
                                        } else {
                                            entityName = (String) pop;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Clear otherwise
                    args.clear();
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            };
        } else {
            return super.visitMethod(access, methodName, desc, signature, exceptions);
        }
    }
}
