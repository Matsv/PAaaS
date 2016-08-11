package nl.matsv.paaaas.data.minecraft;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinecraftVersion {
    private String id;
    private String type;
    private String time;
    private String releaseTime;
    private String url;
}
