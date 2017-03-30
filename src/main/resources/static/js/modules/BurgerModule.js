/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

var burgerModule = {
    register: function () {
        // Load scripts
        moduleManager.loadScript("./js/modules/burger/PacketParser.js");
        moduleManager.loadScript("./js/modules/burger/PacketHandler.js");
    },
    onCompare: function (oldV, newV) {
        this.setProtocolId(oldV.protocol, newV.protocol);
        var packets = packetHandler.handle(oldV, newV);

        var packetDiv = this.getPacketDiv();
        for (var stateKey in packets) {
            var stateDiv = this.addState(stateKey, packetDiv);

            for (var boundKey in packets[stateKey]) {
                var boundDiv = this.addBound(boundKey, stateKey, stateDiv);
                var added = 0;
                for (var packet in packets[stateKey][boundKey]) {
                    var packetObj = packets[stateKey][boundKey][packet];
                    this.addPacket(packetObj, boundDiv, this.getPacketName(oldV.wiki_data, packetObj.old), this.getPacketName(newV.wiki_data, packetObj.new));
                    added++;
                }

                if (added == 0) {
                    web.createElement("p", "nochange", "No changes detected", boundDiv)
                }
            }
        }
    },
    getPacketDiv: function () {
        var row = web.createElementId("div", "row", "", document.getElementById("data"));
        var col = web.createElement("div", "col-lg", "", row);
        var panel = web.createElement("div", "panel panel-info", "", col);
        web.createElement("div", "panel-heading", "<strong>PACKETS</strong>", panel);
        return web.createElement("div", "panel-body", "", panel);
    },
    addState: function (state, packetDiv) {
        var stateDiv = web.createElement("div", state, "", packetDiv);
        var headerClaz = web.createElement("div", "page-header stateHead", "", stateDiv);
        headerClaz.id = state.toLowerCase() + "_header";
        var stateName = web.createElementId("h4", state, state, headerClaz);
        stateName.id = state;
        return stateDiv;
    },
    addBound: function (bound, state, stateDiv) {
        var boundDiv = web.createElement("div", "bounding", "", stateDiv);
        web.createElementId("h5", state + "_" + bound, "<b>" + bound + "</b>", boundDiv);
        return boundDiv;
    },
    setProtocolId: function (oldId, newId) {
        $("#pidOld").html(oldId);
        $("#pidNew").html(newId);
    },
    addPacket: function (json, parent, oldName, newName) {
        var oldData = this.generateInstructions(json.old, "NON-EXISTENT", "danger", oldName);
        var newData = this.generateInstructions(json.new, "REMOVED", "success", newName);

        web.createDifferenceBox(
            oldData.title,
            oldData.body,
            newData.title,
            newData.body,
            parent,
            "packetBox"
        );
    },
    getPacketName: function (wiki, packet) {
        if (wiki != undefined && packet.id != -1) {
            if (packet.state.toLowerCase() in wiki.packetNames) {
                var state = wiki.packetNames[packet.state.toLowerCase()];
                if (packet.direction.toLowerCase() in state) {
                    var direction = state[packet.direction.toLowerCase()];
                    if (packet.id in direction) {
                        return direction[packet.id];
                    }
                }
            }
        }
    },
    generateInstructions: function (packet, title, style, name) {
        var data = {};
        if (packet.id === -1) {
            data.title = "<strong>{0}</strong>".format(title);
            data.body = "";
        } else {
            data.title = this.getPacketTitle(packet, name);
            data.body = undefined;
            if (packet.instructions != undefined) {
                var table = web.createElement("table", "instructionTable", "");
                var tBody = web.createElement("tbody", "packetBody", "", table);

                new packetParser(tBody, style).parsePackets(packet.instructions);
                data.body = table;
            }
        }
        return data;
    },
    // TODO IMPROVE DISPLAY
    getPacketTitle: function (packet, packetName) {
        return "<strong>" + packet.state + ": </strong>" + packet.direction + " <ins>0x" + Number(packet.id).toString(16) + "</ins> -><i><strong>" + (packetName != undefined ? packetName : "") + "</strong></i> (" + packet.class + ")";// TODO
    }
};
