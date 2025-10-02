package hexlet.code.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class WelcomeController {
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Spring";
    }
}
