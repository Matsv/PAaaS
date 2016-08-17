/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

// Based of Burger Vitrine
var dataTypes = {
    data: new HashMap(),
    registerTypes: function () {
        this.data.put("byte", "writeByte");
        this.data.put("boolean", "writeBoolean");
        this.data.put("short", "writeShort");
        this.data.put("int", "writeInt");
        this.data.put("varint", "writeVarInt");
        this.data.put("chatcomponent", "writeChatComponent");
        this.data.put("position", "writePosition");
        this.data.put("enum", "writeVarIntEnum");
        this.data.put("float", "writeFloat");
        this.data.put("long", "writeLong");
        this.data.put("double", "writeDouble");
        this.data.put("string16", "writeString");
        this.data.put("string8", "writeStringUTF");
        this.data.put("byte[]", "writeBytes");
        this.data.put("itemstack", "writeItemStack");
        this.data.put("metadata", "writeMetadata");
        this.data.put("uuid", "writeUUID");
    },

    contains: function (name) {
        var contains = typeof this.data.get(name) != 'undefined';
        if (!contains) console.error("UNKNOWN TYPE " + name);

        return contains;
    },

    get: function (name) {
        return this.data.get(name);
    }
};

var htmlParser = {
    getInstructions: function (instructions, level) {
        var close = false;
        var caz = false;

        var html = [];
        for (var instr in instructions) {
            var output = this.getInstruction(instructions[instr], close, caz, level);
            close = output.close;
            caz = output.caz;
            html.push(output.data);
        }
        if (close)
            this.addLine(html, level, true, "}");

        return html.join("");
    },
    getInstruction: function (instruction, close, caz, level) {
        var html = [];
        if (caz)
            level += 1;
        if (close && !caz)
            this.addLine(html, level, true, "}");
        close = true;
        switch (instruction.operation) {
            case "write":
                this.addLine(html, level, true, "{0}({1});",
                    [
                        dataTypes.contains(instruction.type) ? dataTypes.get(instruction.type) : instruction.type,
                        instruction.field
                    ]);
                close = false;
                break;
            case "if":
                this.addLine(html, level, true, "if({0}) {", [instruction.condition]);
                this.addLine(html, level, false, this.getInstructions(instruction.instructions, level + 1));
                break;
            case "else":
                this.addLine(html, level, true, "} else {");
                this.addLine(html, level, false, this.getInstruction(instruction.instructions, level + 1));
                break;
            case "yloop":
            case "loop":
                this.addLine(html, level, true, "while({0}) {", [instruction.condition]);

                this.addLine(html, level, false, this.getInstructions(instruction.instructions, level + 1));
                break;
            case "switch":
                this.addLine(html, level, true, "switch({0}) {", [instruction.field]);
                this.addLine(html, level, false, this.getInstructions(instruction.instructions, level + 1));
                break;
            case "case":
                if (caz)
                    level -= 1;
                this.addLine(html, level, true, "case {0}:", [instruction.value]);
                close = false;
                caz = true;
                break;
            case "increment":
                if (instruction.amount == 1)
                    this.addLine(html, level, true, "{0}++;", [instruction.field]);
                else
                    this.addLine(html, level, true, "{0} += {1};", [instruction.field, instruction.amount]);
                close = false;
                break;
            case "store":
                this.addLine(html, level, true, "{0} {1} = {2};", [instruction.type, instruction.var, instruction.value]);
                close = false;
                break;
            case "break":
                this.addLine(html, level, true, "break;");
                break;
            default:
                this.addLine(html, level, true, "// {0}", [instruction]);
                break;
        }
        return {data: html.join(""), close: close, caz: caz};
    },
    // TODO is there a better way to do this?
    addLine: function (html, level, newLine, s, objs) {
        html.push(this.getSpaces(level, s.formatArg(objs)));
        if (newLine)
            html.push("<br/>");
    },
    getSpaces: function (amount, value) {
        if (amount > 0) {
            value = "<span class=\"space\">{0}</span>".formatArg([value]);
            return this.getSpaces(--amount, value);
        }
        return value;
    }
};


dataTypes.registerTypes();