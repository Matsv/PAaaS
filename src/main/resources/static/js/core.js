/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
if (!String.prototype.format) {
    String.prototype.format = function () {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function (match, number) {
            return typeof args[number] != 'undefined' ? args[number] : match;
        });
    };
}
String.prototype.formatArg = function (args) {
    return this.replace(/{(\d+)}/g, function (match, number) {
        return typeof args[number] != 'undefined' ? args[number] : match;
    });
};

// TODO MAKE THIS WHOLE PART BETTER, I COPIED THIS FROM THE PROTOTYPE.
var web = {
    isHidden: false,
    registerListeners: function () {
        $('#compare').click(function () {
            var compare = $("#compare");
            compare.html("<span class=\"glyphicon glyphicon-refresh glyphicon-refresh-animate\"></span> Compare versions"); // TODO FIND A BETTER WAY FOR THIS
            compare.prop("disabled", true);

            var oldV = $("#old").find('option:selected').val();
            var newV = $("#new").find('option:selected').val();

            dataHandler.requestCompare(oldV, newV);
        })
    },
    addInfo: function (oldVersion, newVersion) {
        var title = "<strong>INFORMATION</strong>";
        var template = "<strong>Minecraft Version: </strong>{0} <br/> \
        <strong>Version Type: </strong>{1} <br/> \
        <strong>Version release time: </strong>{2} <br/> \
        <strong>Protocol id: </strong><span id={3}>Unknown</span>";

        this.addHtml
        (title,
            template.format(oldVersion.id, oldVersion.type, oldVersion.releaseTime, "pidOld"),
            title,
            template.format(newVersion.id, newVersion.type, newVersion.releaseTime, "pidNew"));
    },
    addPacket: function (oldVersion, newVersion) {
        var oldTitle, oldData, newTitle, newData;
        if (typeof oldVersion === 'undefined') {
            oldTitle = "<strong>NON-EXISTENT</strong>";
            oldData = "";
        } else {
            oldTitle = this.getPacketTitle(oldVersion);
            oldData = htmlParser.getInstructions(oldVersion.instructions, 0);
        }
        if (typeof newVersion === 'undefined') {
            newTitle = "<strong>REMOVED</strong>";
            newData = "";
        } else {
            newTitle = this.getPacketTitle(newVersion);
            newData = htmlParser.getInstructions(newVersion.instructions, 0);
        }


        this.addHtml(
            oldTitle,
            oldData,
            newTitle,
            newData
        );
    },
    getPacketTitle: function (packet) {
        return "<strong>" + packet.state + ": </strong><ins>0x" + Number(packet.id).toString(16) + "</ins> (" + packet.class + ") - " + packet.direction
    },
    setProtocolId: function (oldId, newId) {
        $("#pidOld").html(oldId);
        $("#pidNew").html(newId);
    },
    addHtml: function (title, footer, newTitle, newFooter) {
        $("#data")
            .append("<div class=\"row\">"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-danger\">"
                + "           <div class=\"panel-heading\">" + title + "</div>"
                + "           <div class=\"panel-footer\">" + footer + "</div>"
                + "       </div>"
                + "    </div>"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-success\">"
                + "           <div class=\"panel-heading\">" + newTitle + "</div>"
                + "           <div class=\"panel-footer\">" + newFooter + "</div>"
                + "       </div>"
                + "    </div>");
    },
    addMetadata: function (oldV, newV) {
        $("#data")
            .append("<div class=\"row\">"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-danger\">"
                + "           <div class=\"panel-heading\">Metadata</div>"
                + "           <div class=\"panel-footer oldMeta\"><div id=\"tree\"></div></div>"
                + "       </div>"
                + "    </div>"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-success\">"
                + "           <div class=\"panel-heading\">Metadata</div>"
                + "           <div class=\"panel-footer newMeta\"><div id=\"tree\"></div></div>"
                + "       </div>"
                + "    </div>");
        web.generateTree(oldV, newV);
    },

    generateTree: function (oldV, newV) {
        var oldTree = oldV;
        var newTree = newV;
        web.convertTree(oldTree);
        web.convertTree(newTree);
        console.info([newTree]);
        $('.oldMeta #tree').treeview({data: [JSON.parse(JSON.stringify(oldTree))], levels: 999, showTags: true}); // TODO GET IT WORKING WITHOUT THIS HACKY FIX
        $('.newMeta #tree').treeview({data: [JSON.parse(JSON.stringify(newTree))], levels: 999, showTags: true});
    },
    parseType: function(desc) {
        if (desc.charAt(0) == "[") {
            return web.parseType(desc.substring(6, desc.length - 3)) + " Array";
        } else {
            var type = desc.substring(5, desc.length - 3);
            if(type.substring(0, 31) == "com/google/common/base/Optional") {
                return "Optional " + type.substring(33, type.length - 2);
            }
            return type;
        }
    },
    convertMeta: function (meta) {
        meta.text = "<b>" + meta.index + ".</b> " + web.parseType(meta.type);

        delete meta.index;
        delete meta.field;
        delete meta.function;
        delete meta.type;
        return meta;
    },
    convertTree: function (tree) {
        tree.nodes = [];
        for (var i in tree.metadata) {
            tree.nodes.push(web.convertMeta(tree.metadata[i]));
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
            tree.nodes.push(web.convertTree(tree.children[i]));
        }
        if (tree.entityName == "") {
            tree.text = "Unknown (" + tree.className + ")"
        } else {
            tree.text = tree.entityName + " (" + tree.className + ")"
        }
        tree.icon = "fa fa-smile-o";
        tree.backColor = "#eff4ff";
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

var dataHandler = {
    requestCompare: function (oldV, newV) {
        $.ajax({
            dataType: "json",
            type: "GET",
            url: "./v1/compare",
            data: {"old": oldV, "new": newV},
            success: function (json) {
                moduleManager.execute(json);
            },
            error: function (err) {
                console.log(err);
            }
        });
    }
};

var moduleManager = {
    modules: {},
    on: function (name, func) {
        console.log("register module " + name);
        moduleManager.modules[name] = func;
    },

    execute: function (json) {
        // console.log(JSON.stringify(data));

        Object.keys(json.oldVersion).forEach(function (key, index) {
            if (!moduleManager.modules.hasOwnProperty(key))
                console.error("No module found " + key + " [" + JSON.stringify({}) + "]");
            else
                moduleManager.modules[key](json.oldVersion[key], json.newVersion[key]);
        });

        web.isHidden = true;
        $("#beginModal").modal("hide");
    }
};

function registerModules() {
    // info about the jar
    moduleManager.on("JarModule", function (oldV, newV) {
        web.addInfo(oldV, newV);
    });

    moduleManager.on("BurgerModule", function (oldV, newV) {
        web.setProtocolId(oldV.protocol, newV.protocol);
        Object.keys(oldV.changedPackets).forEach(function (key, index) {
            web.addPacket(oldV.changedPackets[key], newV.changedPackets[key]);
        });
    });

    moduleManager.on("MetadataModule", function (oldV, newV) {
        web.addMetadata(oldV, newV)
    })
}
$(document).ready(function () {
    web.registerListeners();
    registerModules();

    $('#beginModal').modal('show').on('hide.bs.modal', function (e) {
        if (web.isHidden)
            return;
        e.preventDefault();
    });

    if (/Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent)) {
        $('.selectpicker').selectpicker('mobile');
    }
});
