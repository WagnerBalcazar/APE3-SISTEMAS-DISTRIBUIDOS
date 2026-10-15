package edu.ape3.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class MicroserviceController {
    private final String service;
    private final String exercise;

    public MicroserviceController(@Value("${service.number}") String number, @Value("${service.exercise}") String exercise) {
        this.service = "microservicio-" + number;
        this.exercise = exercise;
    }

    @RequestMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", service, "status", "ok", "exercise", exercise);
    }

    @RequestMapping({"/api", "/api/**"})
    public Map<String, String> api() {
        return Map.of("service", service, "exercise", exercise, "message", "Respuesta del microservicio");
    }
}
