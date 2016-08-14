package nl.matsv.paaaas.module.modules.metadata;

import java.util.ArrayList;

class ClassTree {
    private String name;
    private ArrayList<ClassTree> children = new ArrayList<>();

    public ClassTree(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ClassTree> getChildren() {
        return children;
    }

    public boolean contains(String name) {
        for (ClassTree item : children) {
            if (item.getName().equals(name)) return true;
            if (item.contains(name)) return true;
        }
        return false;
    }

    public ClassTree find(String name) {
        for (ClassTree item : children) {
            if (item.getName().equals(name)) return item;
            if (item.contains(name)) return item.find(name);
        }
        return null;
    }

    public boolean insert(String superclass, String name) {
        if (getName().equals(superclass)) {
            children.add(new ClassTree(name));
            return true;
        } else {
            for (ClassTree item : children) {
                if (item.insert(superclass, name))
                    return true;
            }
        }
        return false;
    }
}
