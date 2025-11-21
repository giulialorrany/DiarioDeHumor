package org.example.diariodehumor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String redirectToIndex() {
        return "redirect:/view/index.html";
    }
}
