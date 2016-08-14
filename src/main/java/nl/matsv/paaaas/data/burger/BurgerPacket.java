package nl.matsv.paaaas.data.burger;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
}
