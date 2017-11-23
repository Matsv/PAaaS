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

        var oldPackets = {};
        var newPackets = {};

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
                oldPackets[states[sta]] = {};
                newPackets[states[sta]] = {};

                for (var dire in directions)
                    if (directions.hasOwnProperty(dire)) {
                        packets[states[sta]][directions[dire]] = [];
                        oldPackets[states[sta]][directions[dire]] = [];
                        newPackets[states[sta]][directions[dire]] = [];
                    }
            }
        }
        // Sort packets into the direction & state
        for (var p in oldJson.changedPackets) {
            var val = oldJson.changedPackets[p];
            oldPackets[val.state][val.direction][val.id] = val;
        }
        for (var p in newJson.changedPackets) {
            var val = newJson.changedPackets[p];
            newPackets[val.state][val.direction][val.id] = val;
        }
        // Calculate the correct packets


        for (var sta in states) {
            if (states.hasOwnProperty(sta)) {
                for (var dire in directions)
                    if (directions.hasOwnProperty(dire)) {
                        // Reorder
                        this.reorder(oldPackets[states[sta]][directions[dire]], newPackets[states[sta]][directions[dire]]);
                        // Compare both sides to not skip removed / added instructions
                        this.compare(oldPackets[states[sta]][directions[dire]], newPackets[states[sta]][directions[dire]]);
                        this.compare(newPackets[states[sta]][directions[dire]], oldPackets[states[sta]][directions[dire]]);
                        // Output
                        for (var key in oldPackets[states[sta]][directions[dire]]) {
                            var value = oldPackets[states[sta]][directions[dire]][key];
                            if (value == undefined) continue;
                            var newId = value.newId;
                            var loc = packets[value.state][value.direction];

                            var output = {"old": value};

                            if (newId != undefined) {
                                // don't display if nothing has changed
                                output.new = newPackets[states[sta]][directions[dire]][newId];
                                delete newPackets[states[sta]][directions[dire]][newId];
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

                            if (output.new != undefined && output.new.id != -1) {
                                loc[output.new.id + "a"] = output;
                            } else {
                                loc[value.id + "b"] = output;
                            }

                        }
                        if (Object.keys(newPackets[states[sta]][directions[dire]]).length > 0) {
                            for (var newKey in newPackets[states[sta]][directions[dire]]) {
                                var val = newPackets[states[sta]][directions[dire]][newKey];
                                packets[val.state][val.direction][val.id] = {
                                    old: {
                                        id: -1
                                    },
                                    new: val
                                };
                            }
                        }
                    }
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
        // Basic algorithm:
        var currentShift = 0;
        for (var packet1 in oldP) {
            if (oldP[packet1].newId != undefined) {
                continue;
            }

            // We just want to check if the packet with the id + shift is the same
            var id = parseInt(packet1) + currentShift;
            if (this.isSame(oldP[packet1], newP[id])) {
                oldP[packet1].newId = id;
                newP[id].newId = packet1;
                continue;
            } else {
                // Check if it's now the next packet
                id++;
                if (this.isSame(oldP[packet1], newP[id])) {
                    // Must be a new packet so increment shift
                    currentShift++;
                    oldP[packet1].newId = id;
                    newP[id].newId = packet1;
                } else {
                    currentShift--; // Packet removed
                }
            }
        }
    },

    isSame: function (packet1, packet2) {
        if (packet1 == undefined || packet2 == undefined) return false;
        if (packet1.direction != packet2.direction || packet1.state != packet2.state) {
            return false;
        }
        if ((packet1.instructions != undefined) != (packet2.instructions != undefined)) {
            return false;
        }
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
    },

    // Do all the magic to check if the instructions are equal
    equalsInstruction: function (o1, o2) {
        if (o1 === o2) return true;
        if (o1 == undefined || o2 == undefined) return false;

        if (o1.operation != undefined ? o1.operation !== o2.operation : o2.operation != undefined) return false;
        if (o1.type != undefined ? o1.type !== o2.type : o2.type != undefined) return false;
        // if (o1.var != undefined ? o1.var !== o2.var : o2.var != undefined) return false;
        if (o1.amount != undefined ? o1.amount !== o2.amount : o2.amount != undefined) return false;
        return o1.instructions != undefined ? this.equalsInstruction(o1.instructions, o2.instructions) : o2.instructions == null;
    }
};