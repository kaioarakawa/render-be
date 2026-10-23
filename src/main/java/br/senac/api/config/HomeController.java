package br.senac.api.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "aplicacao", "API Senac",
                "status", "online",
                "documentacao", "/swagger-ui.html",
                "health", "/actuator/health");
    }
}
