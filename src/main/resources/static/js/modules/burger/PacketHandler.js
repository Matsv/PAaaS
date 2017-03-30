/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
// Please give us the has method, we need it
function has(arr, value) {
    for (var i = 0; i < arr.length; i++)
        if (arr[i] === value)
            return true;
    return false;
}

// The equals method is also very important in my life
function equals(obj, other) {
    if (obj.length !== other.length)
        return false;

    for (var i = obj.length; i--;)
        if (obj[i] !== other[i])
            return false;
    return true;
}

// TODO improve api output to make this less complex
var packetHandler = {
    handle: function (oldJson, newJson) {
        var packets = {};

        // Get all the states
        var states = oldJson.states;
        if (!equals(oldJson.states, newJson.states)) {
            for (var state in newJson.states)
                if (newJson.states.hasOwnProperty(state) && !has(states, newJson.states[state]))
                    states.push(newJson.states[state]);
        }

        // Get all the directions
        var directions = oldJson.directions;
        if (!equals(oldJson.directions, newJson.directions)) {
            for (var dir in newJson.directions)
                if (newJson.directions.hasOwnProperty(dir) && !has(directions, newJson.directions[dir]))
                    directions.push(newJson.directions[dir]);
        }

        // Add all the states and directions
        for (var sta in states) {
            if (states.hasOwnProperty(sta)) {
                packets[states[sta]] = {};
                for (var dire in directions)
                    if (directions.hasOwnProperty(dire))
                        packets[states[sta]][directions[dire]] = {};
            }
        }
        // Remap arrays to be id indexed
        var temp = [];
        for (var key in oldJson.changedPackets) {
            temp[oldJson.changedPackets[key].id] = oldJson.changedPackets[key];
        }
        oldJson.changedPackets = temp;

        var temp = [];
        for (var key in newJson.changedPackets) {
            temp[newJson.changedPackets[key].id] = newJson.changedPackets[key];
        }
        newJson.changedPackets = temp;


        // Calculate the correct packets
        this.reorder(oldJson.changedPackets, newJson.changedPackets);
        // Compare both sides to not skip removed / added instructions
        this.compare(oldJson.changedPackets, newJson.changedPackets);
        this.compare(newJson.changedPackets, oldJson.changedPackets);

        for (var key in oldJson.changedPackets) {
            var value = oldJson.changedPackets[key];
            var newId = value.newId;
            var loc = packets[value.state][value.direction];

            var output = {"old": value};

            if (newId != undefined) {
                // don't display if nothing has changed
                output.new = newJson.changedPackets[newId];
                delete newJson.changedPackets[newId];
                if (this.isSame(output.new, value)) {
                    if (output.new.id == value.id) {
                        continue;
                    } else {
                        output.new["instructions"] = undefined;
                        output.old["instructions"] = undefined;
                    }
                }
            } else {
                output.new = {
                    id: -1
                }
            }
            loc[output.new.id] = output;
        }
        if (Object.keys(newJson).length > 0) {
            for (var newKey in newJson.changedPackets) {
                var val = newJson.changedPackets[newKey];
                packets[val.state][val.direction][val.id] = {
                    old: {
                        id: -1
                    },
                    new: val
                };
            }
        }
        return packets;
    },

    // Compare the packet instructions to have fancy diff view
    compare: function (oldP, newP) {
        for (var packet in oldP) {
            if (!("instructions" in oldP[packet])) continue;
            var newId = oldP[packet].newId;
            for (var instr in oldP[packet]["instructions"]) {
                if (newP[newId] == undefined || newP[newId]["instructions"] == undefined)
                    oldP[packet]["instructions"][instr].changed = true;
                else
                    oldP[packet]["instructions"][instr].changed = !this.equalsInstruction(oldP[packet]["instructions"][instr], newP[newId]["instructions"][instr]);
            }
        }
    },

    // Reorder the packets so that they match up
    reorder: function (oldP, newP) {
        // First iteration matches identical packets
        loop1:
            for (var packet1 in oldP) {
                if (oldP[packet1].newId != undefined) {
                    continue loop1;
                }
                // Search
                for (var packet2 in newP) {
                    if (this.isSame(oldP[packet1], newP[packet2]) && newP[packet2].newId == undefined) {
                        oldP[packet1].newId = newP[packet2].id;
                        newP[packet2].newId = oldP[packet1].id;
                        continue loop1;
                    }
                }
            }
        // Second iteration (attempts to find closest match)
        loop1:
            for (var packet1 in oldP) {
                if (oldP[packet1].newId != undefined) continue loop1;
                // Search
                var highest = undefined;
                var score = Number.MAX_VALUE;
                loop2:
                    for (var packet2 in newP) {
                        if (newP[packet2].newId != undefined) continue loop2;

                        // Generate a score for this packet
                        var totalDiff = Math.abs(oldP[packet1]["instructions"].length - newP[packet2]["instructions"].length)
                        var instrDiff = 0;
                        for (var instr in oldP[packet1]["instructions"]) {
                            if (!this.equalsInstruction(oldP[packet1]["instructions"][instr], newP[packet2]["instructions"][instr])) {
                                instrDiff++;
                            }
                        }
                        var idDiff = Math.abs(oldP[packet1].id - newP[packet2].id);

                        var weighted = (4 * idDiff) + (2 * instrDiff) * (1 * totalDiff)

                        if (weighted < score) {
                            score = weighted;
                            highest = newP[packet2];
                        }
                    }
                // Don't allow it to match any packet (must have a score less than this)
                var max = (4 * 5) + (2 * 3) + (1 * 2);
                if (score < max) {
                    if (highest != undefined) {
                        oldP[packet1].newId = highest.id;
                        highest.newId = oldP[packet1].id;
                    }
                }
            }
    },

    isSame: function (packet1, packet2) {
        for (var instr in packet1["instructions"]) {
            if (!this.equalsInstruction(packet1["instructions"][instr], packet2["instructions"][instr])) {
                return false;
            }
        }
        for (var instr in packet2["instructions"]) {
            if (!this.equalsInstruction(packet2["instructions"][instr], packet1["instructions"][instr])) {
                return false;
            }
        }
        return true;
        // return packet1.id == packet2.id;
    },

    // Do all the magic to check if the instructions are equal
    equalsInstruction: function (o1, o2) {
        if (o1 === o2) return true;
        if (o1 == undefined || o2 == undefined) return false;

        if (o1.operation != undefined ? o1.operation !== o2.operation : o2.operation != undefined) return false;
        if (o1.type != undefined ? o1.type !== o2.type : o2.type != undefined) return false;
        if (o1.var != undefined ? o1.var !== o2.var : o2.var != undefined) return false;
        if (o1.amount != undefined ? o1.amount !== o2.amount : o2.amount != undefined) return false;
        return o1.instructions != undefined ? this.equalsInstruction(o1.instructions, o2.instructions) : o2.instructions == null;
    }
};