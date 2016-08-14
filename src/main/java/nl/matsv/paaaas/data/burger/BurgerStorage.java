package nl.matsv.paaaas.data.burger;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class BurgerStorage {
    @SerializedName("class")
    private String claz;
    private String field;
    private String name;
}
