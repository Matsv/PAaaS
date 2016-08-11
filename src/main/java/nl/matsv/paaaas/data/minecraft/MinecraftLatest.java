package nl.matsv.paaaas.data.minecraft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinecraftLatest {
    private String snapshot;
    private String release;
}
