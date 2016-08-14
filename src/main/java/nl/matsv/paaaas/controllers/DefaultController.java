package nl.matsv.paaaas.controllers;

import nl.matsv.paaaas.storage.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {
    @Autowired
    StorageManager storageManager;

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("versions", storageManager.getEnabledVersions());
        return "index";
    }
}
