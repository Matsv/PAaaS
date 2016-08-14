package nl.matsv.paaaas.data.burger;

import lombok.Data;

import java.util.Map;

@Data
public class BurgerPackets {
    private Map<String, BurgerStorage> directions;
    private Map<String, Integer> info;
    private Map<String, BurgerPacket> packet;
    private Map<String, BurgerStorage> states;

}
