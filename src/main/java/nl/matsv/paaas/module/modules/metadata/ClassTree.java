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
