package nl.matsv.paaaas.data.metadata;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class MetadataTree {
    private String className;
    private Optional<String> entityName;

    private List<MetadataTree> children = new ArrayList<>();
    private List<MetadataEntry> metadata = new ArrayList<>();
}
