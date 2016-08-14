package nl.matsv.paaaas.data.burger;

import lombok.Data;

import java.util.Map;

@Data
public class BurgerOutput {
    private Map<String, String> classes;
    private BurgerPackets packets;
    private Map<String, String> source;
    private Map<String, String> version;

}
