package tech.agrowerk.application.controller.fallback;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaFallbackController {

    @GetMapping(value = "/{path:[^\\.]*}/**")
    public String redirect() {
        return "forward:/index.html";
    }
}