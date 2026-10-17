package edu.ape3.microservicio5;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Microservicio5Application {
    public static void main(String[] args) { SpringApplication.run(Microservicio5Application.class, args); }
}

@RestController
@CrossOrigin(origins = "*")
class Microservicio5Controller {
    @RequestMapping("/health")
    Map<String, String> health() { return Map.of("service", "microservicio-5", "status", "ok", "exercise", "Caso integrador"); }

    @RequestMapping({"/api", "/api/**"})
    Map<String, String> api() { return Map.of("service", "microservicio-5", "exercise", "Caso integrador", "message", "Respuesta del microservicio 5"); }
}
