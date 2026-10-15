package edu.ape3.microservicio1;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class ParkingSimulationController {
    private final ParkingSimulationService simulationService;

    public ParkingSimulationController(ParkingSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "microservicio-1",
                "status", "ok",
                "exercise", "Control de acceso a un estacionamiento");
    }

    @GetMapping({"/api", "/api/simular"})
    public ResponseEntity<ParkingSimulationService.SimulationResult> simulate() {
        return ResponseEntity.ok(simulationService.simulate(4, 10));
    }
}
