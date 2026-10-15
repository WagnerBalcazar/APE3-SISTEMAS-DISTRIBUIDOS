package edu.ape3.microservicio4;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Microservicio4Application {
    public static void main(String[] args) { SpringApplication.run(Microservicio4Application.class, args); }
}

@RestController
@CrossOrigin(origins = "*")
class Microservicio4Controller {
    @RequestMapping("/health")
    Map<String, String> health() { return Map.of("service", "microservicio-4", "status", "ok", "exercise", "Consistencia de datos"); }

    @RequestMapping({"/api", "/api/**"})
    Map<String, String> api() { return Map.of("service", "microservicio-4", "exercise", "Consistencia de datos", "message", "Respuesta del microservicio 4"); }
}
