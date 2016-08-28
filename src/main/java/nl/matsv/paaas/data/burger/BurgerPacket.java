/*
 * Copyright (c) 2016 Mats & Myles
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.paaas.data.burger;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class BurgerPacket {
    @SerializedName("class")
    private String claz;
    private String direction;
    private String state;
    private boolean from_client;
    private boolean from_server;
    private int id;
    private List<BurgerInstruction> instructions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BurgerPacket that = (BurgerPacket) o;

        if (from_client != that.from_client) return false;
        if (from_server != that.from_server) return false;
        if (id != that.id) return false;
        if (!direction.equals(that.direction)) return false;
        if (!state.equals(that.state)) return false;
        return instructions != null ? instructions.equals(that.instructions) : that.instructions == null;

    }

    @Override
    public int hashCode() {
        int result = direction.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + (from_client ? 1 : 0);
        result = 31 * result + (from_server ? 1 : 0);
        result = 31 * result + id;
        result = 31 * result + (instructions != null ? instructions.hashCode() : 0);
        return result;
    }
}
