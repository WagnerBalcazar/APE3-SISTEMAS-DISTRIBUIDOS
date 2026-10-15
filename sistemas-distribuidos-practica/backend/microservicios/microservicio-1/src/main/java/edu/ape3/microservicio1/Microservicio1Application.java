package edu.ape3.microservicio1;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Microservicio1Application {
    public static void main(String[] args) { SpringApplication.run(Microservicio1Application.class, args); }
}

@RestController
@CrossOrigin(origins = "*")
class Microservicio1Controller {
    @RequestMapping("/health")
    Map<String, String> health() { return Map.of("service", "microservicio-1", "status", "ok", "exercise", "Comunicacion entre procesos"); }

    @RequestMapping({"/api", "/api/**"})
    Map<String, String> api() { return Map.of("service", "microservicio-1", "exercise", "Comunicacion entre procesos", "message", "Respuesta del microservicio 1"); }
}
