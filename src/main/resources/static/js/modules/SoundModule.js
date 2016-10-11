/*
 * Copyright (c) 2016 Mats & Myles
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

var soundModule = {
    register: function () {

    },
    onCompare: function (oldV, newV) {
        var row = web.createElementId("div", "row", "", document.getElementById("data"));
        var col = web.createElement("div", "col-lg", "", row);
        var panel = web.createElement("div", "panel panel-success", "", col);
        web.createElement("div", "panel-heading", "<strong>SOUNDS</strong>", panel);
        var body = web.createElement("div", "panel-body", "", panel);
        var shift = 0;
        var shiftMessage = false;
        var changes = 0;
        var max = Math.max(newV.length, oldV.length);
        for (var i = 0; i < max; i++) {
            if (i < oldV.length) {
                var newIndex = newV.indexOf(oldV[i]);
                if (newIndex != i) {
                    if (newIndex == -1) {
                        web.createElement("div", "text-danger", "<strong>Old: " + i + " was removed - " + oldV[i] + "</strong>", body);
                        changes++;
                        shift -= 1;
                    }
                }
            }
            if (i < newV.length) {
                var oldIndex = oldV.indexOf(newV[i]);
                if (oldIndex != i) {
                    if (oldIndex == -1) {
                        web.createElement("div", "text-success", "<strong>New: " + i + " - " + newV[i] + "</strong>", body);
                        shift += 1;
                        shiftMessage = false;
                        changes++;
                    } else {
                        // Check if it's simply a shift
                        if (shift != 0) {
                            if (!shiftMessage) {
                                shiftMessage = true;
                                web.createElement("div", "text-warning", "<strong>Old: " + oldIndex + " and above, moved to " + i + " (from " + newV[i] + ")</strong>", body);
                                changes++;
                            }
                        } else {
                            web.createElement("div", "text-danger", "<strong>Old: " + oldIndex + " moved to " + i + " - " + newV[i] + "</strong>", body);
                            changes++;
                        }
                    }
                }
            }
        }
        if (changes == 0) {
            web.createElement("div", "text-info", "<strong>No sounds changed!</strong>", body);
        }
        return body;
    }
};
