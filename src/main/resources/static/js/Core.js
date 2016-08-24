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

var web = {
    isHidden: false,
    registerListeners: function () {
        // Compare button
        $('#compare').click(function () {
            var compare = $("#compare");
            compare.html("<span class=\"glyphicon glyphicon-refresh glyphicon-refresh-animate\"></span> Compare versions"); // TODO FIND A BETTER WAY FOR THIS
            compare.prop("disabled", true);

            var oldV = $("#old").find('option:selected').val();
            var newV = $("#new").find('option:selected').val();

            dataHandler.requestCompare(oldV, newV);
        });

        // Prevent modal closing
        $('#beginModal').modal('show').on('hide.bs.modal', function (e) {
            if (web.isHidden)
                return;
            e.preventDefault();
        });
    },
    addHtml: function (title, footer, newTitle, newFooter, parent) {
        var data = document.getElementById("data");
        if (parent != undefined) {
            data = parent;
        }

        var row = this.createElement("div", "row", "", data);
        this.setInner("panel-danger", title, footer, row);
        this.setInner("panel-success", newTitle, newFooter, row);
    },
    setInner: function (claz, title, footer, sub) {
        var col = this.createElement("div", "col-md-6", "", sub);
        var panel = this.createElement("div", "panel " + claz, "", col);
        this.createElement("div", "panel-heading", title, panel);
        this.createElement("div", "panel-body", footer, panel);
    },
    createElement: function (type, claz, value, sub) {
        var el = document.createElement(type);
        el.className = claz;
        if (typeof value !== "undefined") { // TODO Do research to find the correct way to check this.
            el.innerHTML = value;
        }
        if (typeof sub !== "undefined") {
            sub.appendChild(el);
        }
        return el;
    },
    createElementId: function (type, id, value, sub) {
        var el = document.createElement(type);
        el.id = id;
        if (value != undefined) { // TODO Do research to find the correct way to check this.
            el.innerHTML = value;
        }
        if (sub != undefined) {
            sub.appendChild(el);
        }
        return el;
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
    /**
     * Register module handlers
     *
     * @param name same name as the module provided by Json
     * @param func return the handle object
     */
    on: function (name, func) {
        this.loadScript("/js/modules/" + name + ".js", function (data, textStatus, xhr) {
            if (textStatus === "success") {
                var obj = func();

                if ("register" in obj) obj.register();
                moduleManager.modules[name] = func();

                console.log("registered module " + name);
            } else {
                console.error("Could not load module {0}: {1}".format(name, textStatus));
                console.error(xhr);
            }
        });
    },

    execute: function (json) {
        Object.keys(json.oldVersion).forEach(function (key) {
            if (!moduleManager.modules.hasOwnProperty(key))
                console.error("No module found " + key + " [" + JSON.stringify({}) + "]");
            else
                moduleManager.modules[key].onCompare(json["oldVersion"][key], json["newVersion"][key]);
        });

        web.isHidden = true;
        $("#beginModal").modal("hide");
    },

    register: function () {
        // info about the jar
        moduleManager.on("JarModule", function () {
            return jarModule;
        });

        // Burger output
        moduleManager.on("BurgerModule", function () {
            return burgerModule;
        });

        // Metadata output
        moduleManager.on("MetadataModule", function () {
            return metadataModule;
        });
    },

    loadScript: function (path, callback) {
        $.getScript(path, callback != undefined ? callback : function (data, textStatus, xhr) {
            if (textStatus !== "success") {
                console.error("Could not load " + path + " " + textStatus);
                console.error(xhr);
            } else {
                console.log("Loaded script " + path);
            }
        });
    }
};

$(document).ready(function () {
    if (/Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent)) {
        $('.selectpicker').selectpicker('mobile');
    }

    web.registerListeners();
    moduleManager.register();

    // Fix anchor things
    var shiftWindow = function () {
        scrollBy(0, -65)
    };
    if (location.hash) shiftWindow();
    window.addEventListener("hashchange", shiftWindow);
});