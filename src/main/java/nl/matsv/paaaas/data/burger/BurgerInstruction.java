package nl.matsv.paaaas.data.burger;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class BurgerInstruction {
    private String operation;

    private String field;
    private String type;
    private String var;

    private Integer amount;
    private String value;
    private String condition;
    private List<BurgerInstruction> instructions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BurgerInstruction that = (BurgerInstruction) o;

        if (operation != null ? !operation.equals(that.operation) : that.operation != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (var != null ? !var.equals(that.var) : that.var != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        return instructions != null ? instructions.equals(that.instructions) : that.instructions == null;

    }

    @Override
    public int hashCode() {
        int result = operation != null ? operation.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (var != null ? var.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (instructions != null ? instructions.hashCode() : 0);
        return result;
    }
}
