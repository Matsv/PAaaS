package nl.matsv.paaaas.controllers;

import nl.matsv.paaaas.data.minecraft.MinecraftVersion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

@Controller
public class DefaultController {

    @RequestMapping("/")
    public String index(Model model) {
        // TODO MAKE REAL DATA
        model.addAttribute("versions", Arrays.asList(
                new MinecraftVersion("16w32a", "snapshot", "2016-08-10T12:30:59+00:00", "2016-08-10T12:30:10+00:00", "https://launchermeta.mojang.com/mc/game/f09fe887f1c78692dddefb0375b7b4c6d90aee72/16w32a.json"),
                new MinecraftVersion("1.10.2", "release", "2016-07-22T08:46:23+00:00", "2016-06-23T09:17:32+00:00", "https://launchermeta.mojang.com/mc/game/1920a2b4e996bae0af1a67d38d63706bac10ac47/1.10.2.json")));
        return "index";
    }
}
