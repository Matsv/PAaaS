package nl.matsv.paaaas.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class VersionMeta {
    private boolean enabled;
    private boolean burger;
    private List<String> errors;

    public void addError(String error){
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        errors.add(format.format(new Date()) + ": " + error);
    }

    public void addErrors(String[] errors){
        for (String err : errors)
            addError(err);
    }
}
