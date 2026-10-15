package edu.ape3.microservicio1;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/api/estacionamiento/estado")
    public ServicioSimulacionEstacionamiento.EstadoEstacionamiento estado() {
        return servicioSimulacion.estadoEstacionamiento();
    }

    @PostMapping("/api/estacionamiento/llegar/{id}")
    public ServicioSimulacionEstacionamiento.EstadoEstacionamiento llegar(@PathVariable int id) {
        return servicioSimulacion.llegarVehiculo(id);
    }

    @PostMapping("/api/estacionamiento/salir/{id}")
    public ServicioSimulacionEstacionamiento.EstadoEstacionamiento salir(@PathVariable int id) {
        return servicioSimulacion.salirVehiculo(id);
    }

    @PostMapping("/api/estacionamiento/reiniciar")
    public ServicioSimulacionEstacionamiento.EstadoEstacionamiento reiniciar() {
        return servicioSimulacion.reiniciarEstacionamiento();
    }
}
