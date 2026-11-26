package com.lesavor.restaurant;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping({"/", "/login", "/menu", "/cart", "/user"})
    public String index() {
        return "forward:/index.html";
    }
}
