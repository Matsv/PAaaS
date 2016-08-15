package nl.matsv.paaaas.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.matsv.paaaas.data.burger.BurgerOutput;
import nl.matsv.paaaas.data.metadata.MetadataTree;
import nl.matsv.paaaas.data.minecraft.MinecraftVersion;

//TODO
@Data
@AllArgsConstructor
public class VersionDataFile {
    private final MinecraftVersion version;
    private VersionMeta metadata;
    private BurgerOutput burgerData;
    private MetadataTree metadataTree;
    // Store executed modules & versions
    // Store module data
}