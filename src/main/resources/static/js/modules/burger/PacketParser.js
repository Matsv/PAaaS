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
    data: {},
    registerTypes: function () {
        this.data["byte"] = "writeByte";
        this.data["boolean"] = "writeBoolean";
        this.data["short"] = "writeShort";
        this.data["int"] = "writeInt";
        this.data["varint"] = "writeVarInt";
        this.data["varint[]"] = "writeVarIntArray";
        this.data["chatcomponent"] = "writeChatComponent";
        this.data["position"] = "writePosition";
        this.data["enum"] = "writeVarIntEnum";
        this.data["float"] = "writeFloat";
        this.data["long"] = "writeLong";
        this.data["varlong"] = "writeVarLong";
        this.data["double"] = "writeDouble";
        this.data["string16"] = "writeString";
        this.data["string8"] = "writeStringUTF";
        this.data["byte[]"] = "writeBytes";
        this.data["itemstack"] = "writeItemStack";
        this.data["metadata"] = "writeMetadata";
        this.data["uuid"] = "writeUUID";
        this.data["nbtcompound"] = "writeNBT";
    },
    contains: function (name) {
        var contains = name in this.data;
        if (!contains) console.error("UNKNOWN TYPE " + name);

        return contains;
    },
    get: function (name) {
        return this.data[name];
    }
};
dataTypes.registerTypes();

var packetParser = function (tbody, style) {
    this.output = tbody;
    this.style = style;
    this.changed = false;

    this.parsePackets = function (instruction) {
        return this.getInstructions(instruction, 0);
    };

    this.getInstructions = function (instructions, level) {
        var close = false;
        var caz = false;

        for (var instr in instructions) {
            var output = this.getInstruction(instructions[instr], close, caz, level);
            close = output.close;
            caz = output.caz;
        }
        if (close)
            this.addLine(level, "}");
    };

    this.getInstruction = function (instruction, close, caz, level) {
        if (caz)
            level += 1;
        if (close && !caz)
            this.addLine(level, "}");
        close = true;
        if ('changed' in instruction)
            this.changed = instruction.changed;
        switch (instruction.operation) {
            case "write":
                this.addLine(level, "{0}({1});",
                    [
                        dataTypes.contains(instruction.type) ? dataTypes.get(instruction.type) : instruction.type,
                        instruction.field
                    ]);
                close = false;
                break;
            case "if":
                this.addLine(level, "if({0}) {", [instruction.condition]);
                this.getInstructions(instruction.instructions, level + 1);
                break;
            case "else":
                this.addLine(level, "} else {");
                this.getInstructions(instruction.instructions, level + 1);
                break;
            case "yloop":
            case "loop":
                this.addLine(level, "while({0}) {", [instruction.condition]);
                this.getInstructions(instruction.instructions, level + 1);
                break;
            case "switch":
                this.addLine(level, "switch({0}) {", [instruction.field]);
                this.getInstructions(instruction.instructions, level + 1);
                break;
            case "case":
                if (caz)
                    level -= 1;
                this.addLine(level, "case {0}:", [instruction.value]);
                close = false;
                caz = true;
                break;
            case "increment":
                if (instruction.amount == 1)
                    this.addLine(level, "{0}++;", [instruction.field]);
                else
                    this.addLine(level, "{0} += {1};", [instruction.field, instruction.amount]);
                close = false;
                break;
            case "store":
                this.addLine(level, "{0} {1} = {2};", [instruction.type, instruction.var, instruction.value]);
                close = false;
                break;
            case "break":
                this.addLine(level, "break;");
                break;
            default:
                this.addLine(level, "// {0}", [instruction]);
                console.warn("Unknown operation: " + instruction.operation);
                break;
        }
        return {close: close, caz: caz};
    };

    this.addLine = function (level, output, objs) {
        if (typeof output === "string")
            this.output.appendChild(this.createTR(this.changed, this.getSpaces(level, output.formatArg(objs))));
        else if (typeof output === "object")
            this.output.appendChild(output);
        else
            console.error("Failed to add a line, unknown type: " + typeof output);
    };
    this.getSpaces = function (amount, value) {
        if (amount > 0) {
            value = web.createElement("span", "space", value);
            return this.getSpaces(--amount, value);
        }
        return value;
    };
    this.createTR = function (changed, value) {
        var tr1 = web.createElement("tr", changed ? this.style : undefined, "");
        web.createElement("td", "packetBox", value, tr1);
        return tr1;
    }
};
