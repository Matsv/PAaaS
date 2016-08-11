package nl.matsv.paaaas;

import nl.matsv.paaaas.data.PAaaSData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataProvider {
    private final Map<String, PAaaSData> versionData = new ConcurrentHashMap<>();

    public boolean contains(String id) {
        return versionData.containsKey(id);
    }

    public PAaaSData get(String id) {
        return versionData.get(id);
    }

    public void put(String id, PAaaSData data) {
        versionData.put(id, data);
    }

    public List<PAaaSData> values(){
        return new ArrayList<>(versionData.values());
    }
}
