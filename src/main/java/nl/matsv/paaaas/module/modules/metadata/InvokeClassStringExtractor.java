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
