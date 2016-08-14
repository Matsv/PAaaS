package nl.matsv.paaaas.data.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetadataEntry {
    private int index = 0;
    private String field;
    private String function;
    private String type;
}
