package nl.matsv.paaaas.data.burger;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class BurgerInstruction {
    private String operation;

    private String field;
    private String type;
    private String var;

    private int amount;
    private String value;
    private String condition;
    private List<BurgerInstruction> instructions;
}
