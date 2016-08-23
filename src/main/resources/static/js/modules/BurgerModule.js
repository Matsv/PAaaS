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

    },
    onCompare: function (oldV, newV) {
        this.setProtocolId(oldV.protocol, newV.protocol);
        Object.keys(oldV.changedPackets).forEach(function (key) {
            burgerModule.addPacket(oldV.changedPackets[key], newV.changedPackets[key]);
        });
    },
    setProtocolId: function (oldId, newId) {
        $("#pidOld").html(oldId);
        $("#pidNew").html(newId);
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


        web.addHtml(
            oldTitle,
            oldData,
            newTitle,
            newData
        );
    },
    getPacketTitle: function (packet) {
        return "<strong>" + packet.state + ": </strong><ins>0x" + Number(packet.id).toString(16) + "</ins> (" + packet.class + ") - " + packet.direction;// TODO
    }
};
