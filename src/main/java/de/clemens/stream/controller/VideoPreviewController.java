package de.clemens.stream.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VideoPreviewController {
    @GetMapping("/video")
    public String greeting(Model model) {
        model.addAttribute("message", "Willkommen auf der Begrüßungsseite!");
        return "index";
    }
}
