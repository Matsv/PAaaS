/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

var metadataModule = {
    onCompare: function (oldV, newV) {
        web.addHtml("<strong>Metadata</strong>", "<div id=\"oldTree\"></div>", "<strong>Metadata</strong>", "<div id=\"newTree\"></div>"); // Todo
        this.generateTree(oldV, newV);
    },
    generateTree: function (oldV, newV) {
        var oldTree = oldV;
        var newTree = newV;
        this.convertTree(oldTree);
        this.convertTree(newTree, oldTree);
        $('#oldTree').treeview({data: [JSON.parse(JSON.stringify(oldTree))], levels: 99, showTags: true}); // TODO GET IT WORKING WITHOUT THIS HACKY FIX
        $('#newTree').treeview({data: [JSON.parse(JSON.stringify(newTree))], levels: 99, showTags: true});
    },
    parseType: function (desc) {
        if (desc.charAt(0) == "[") {
            return this.parseType(desc.substring(6, desc.length - 3)) + " Array";
        } else {
            var type = desc.substring(5, desc.length - 3);
            if (type.substring(0, 31) == "com/google/common/base/Optional") {
                return "Optional " + type.substring(33, type.length - 2);
            }
            return type;
        }
    },
    convertMeta: function (meta, oldTree) {
        meta.text = "<b>" + meta.index + ".</b> " + this.parseType(meta.type);
        meta.data = {type: meta.type, index: meta.index};
        if (oldTree != undefined) {
            if (oldTree != "not_found") {
                var found = false;
                for (var i2 in oldTree.nodes) {
                    if (oldTree.nodes[i2].data.index == meta.index && this.parseType(oldTree.nodes[i2].data.type) == this.parseType(meta.data.type)) {
                        found = true;
                        break;
                    }
                    if (oldTree.nodes[i2].data.index == meta.index && this.parseType(oldTree.nodes[i2].data.type) != this.parseType(meta.data.type)) {
                        meta.backColor = "#AAFA89";
                        break;
                    }
                }
                if (!found)
                    meta.backColor = "#AAFA89";
            } else {
                meta.backColor = "#AAFA89";
            }
        }
        delete meta.index;
        delete meta.field;
        delete meta.function;
        delete meta.type;
        return meta;
    },
    convertTree: function (tree, oldTree) {
        tree.nodes = [];
        tree.data = {entity: tree.entityName}
        for (var i in tree.metadata) {
            tree.nodes.push(this.convertMeta(tree.metadata[i], oldTree));
        }
        // sort the children because we are good parents
        tree.children.sort(function compare(a, b) {
            if (a.entityName == "" && b.entityName == "")
                return a.className.localeCompare(b.className);
            if (a.entityName == "")
                return -1;
            if (b.entityName == "")
                return 1;
            return a.entityName.localeCompare(b.entityName);
        });

        for (var i in tree.children) {
            if (oldTree != undefined) {
                var childTree = "not_found";
                var last = undefined;
                var num = 0;
                if (oldTree != "not_found") {
                    for (var i2 in oldTree.nodes) {
                        if (oldTree.nodes[i2].data.entity != "" && tree.children[i].entityName != "") {
                            if (oldTree.nodes[i2].data.entity == tree.children[i].entityName) {
                                childTree = oldTree.nodes[i2];
                                break;
                            }
                        }
                        if (oldTree.nodes[i2].data.entity != undefined) {
                            last = oldTree.nodes[i2];
                            num++;
                        }
                    }
                    if (oldTree != "not_found" && num == 1 && tree.children.length == 1) {
                        childTree = last; // do do do, inspector gadget
                    }
                }
                tree.nodes.push(this.convertTree(tree.children[i], childTree));
            } else {
                tree.nodes.push(this.convertTree(tree.children[i]));
            }
        }
        if (tree.entityName == "") {
            tree.text = "Unknown (" + tree.className + ")"
        } else {
            tree.text = tree.entityName + " (" + tree.className + ")"
        }
        tree.icon = "fa fa-smile-o";
        if (oldTree == "not_found") {
            tree.backColor = "#d3ffc1";
        } else {
            tree.backColor = "#eff4ff";
        }
        tree.tags = [tree.metadata.length + " tag(s)"];
        delete tree.metadata;
        delete tree.children;
        delete tree.entityName;
        delete tree.className;

        if (tree.nodes.length == 0)
            delete tree.nodes;
        return tree;
    }
};