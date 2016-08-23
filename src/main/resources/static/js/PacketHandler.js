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
            if (states.hasOwnProperty(sta))
                packets[states[sta]] = {};
            for (var dire in directions)
                if (directions.hasOwnProperty(dire))
                    packets[states[sta]][directions[dire]] = {};
        }

        for (var key in oldJson.changedPackets) {
            var value = oldJson.changedPackets[key];
            var loc = packets[value.state][value.direction];

            var output = {"old": value};
            if (key in newJson.changedPackets) {
                output.new = newJson.changedPackets[key];

                delete newJson.changedPackets[key];
            } else {
                output.new = {
                    id: -1
                }
            }

            loc[value.id] = output;
        }

        if (newJson.changedPackets > 0) {
            for (var newKey in newJson.changedPackets) {
                var val = newJson.changedPackets[newKey];
                packets[value.state][value.direction][val.id] = {
                    old: {
                        id: -1
                    },
                    "new": val
                };
            }
        }

        return packets;
    }
};