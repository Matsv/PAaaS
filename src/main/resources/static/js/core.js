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
            newTitle = this.getPacketTitle(oldVersion);
            newData = htmlParser.getInstructions(oldVersion.instructions, 0);
        }


        this.addHtml(
            oldTitle,
            oldData,
            newTitle,
            newData
        );
    },
    getPacketTitle: function (packet) {
        console.log(packet);
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
                + "           <div class=\"panel-footer code\">" + footer + "</div>"
                + "       </div>"
                + "    </div>"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-success\">"
                + "           <div class=\"panel-heading\">" + newTitle + "</div>"
                + "           <div class=\"panel-footer code\">" + newFooter + "</div>"
                + "       </div>"
                + "    </div>");
    },
    addMetadata: function (oldV, newV) {
        $("#data")
            .append("<div class=\"row\">"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-danger\">"
                + "           <div class=\"panel-heading\">Metadata</div>"
                + "           <div class=\"panel-footer code old\"><div id='tree'></div></div>"
                + "       </div>"
                + "    </div>"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-success\">"
                + "           <div class=\"panel-heading\">Metadata</div>"
                + "           <div class=\"panel-footer code new\"><div id='tree'></div></div>"
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
        $('.oldMeta #tree').treeview({data: [JSON.parse(JSON.stringify(oldTree))]}); // TODO GET IT WORKING WITHOUT THIS HACKY FIX
        $('.newMeta #tree').treeview({data: [JSON.parse(JSON.stringify(newTree))]});
    },
    convertMeta: function (meta) {
        meta.text = meta.index + ": " + meta.type;
        meta.nodeId = meta.index;
        delete meta.index;
        delete meta.field;
        delete meta.function;
        delete meta.type;
        return meta;
    },
    convertTree: function (tree) {
        tree.nodes = [];
        for (var i in tree.children) {
            tree.nodes.push(web.convertTree(tree.children[i]));
        }
        for (var i in tree.metadata) {
            tree.nodes.push(web.convertMeta(tree.metadata[i]));
        }
        tree.nodeId = 10;
        delete tree.metadata;
        delete tree.children;
        if (tree.entityName == "") {
            tree.text = "Unknown (" + tree.className + ")"
        } else {
            tree.text = tree.entityName + " (" + tree.className + ")"
        }
        tree.icon = "fa fa-smile-o";
        delete tree.entityName;
        delete tree.className;
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
            console.log(key + " " + moduleManager.modules);
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
        console.log(oldV);
        console.log(newV);
        web.addInfo(oldV, newV);
    });

    moduleManager.on("BurgerModule", function (oldV, newV) {
        web.setProtocolId(oldV.protocol, newV.protocol);
        Object.keys(oldV.changedPackets).forEach(function (key, index) {
            web.addPacket(oldV.changedPackets[key], newV.changedPackets[key]);
        });
    })

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
