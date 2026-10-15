package edu.ape3.microservicio1;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class ControladorEstacionamiento {
    private final ServicioSimulacionEstacionamiento servicioSimulacion;

    public ControladorEstacionamiento(ServicioSimulacionEstacionamiento servicioSimulacion) {
        this.servicioSimulacion = servicioSimulacion;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "microservicio-1",
                "status", "ok",
                "exercise", "Control de acceso a un estacionamiento");
    }

    @GetMapping({"/api", "/api/simular"})
    public ResponseEntity<ServicioSimulacionEstacionamiento.ResultadoSimulacion> simular() {
        return ResponseEntity.ok(servicioSimulacion.simular(4, 10));
    }
}
