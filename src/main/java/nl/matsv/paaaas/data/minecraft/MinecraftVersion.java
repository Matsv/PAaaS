package nl.matsv.paaaas.data.minecraft;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinecraftVersion {
    private String id;
    private String type;
    private Date time;
    private Date releaseTime;
    private String url;
}
