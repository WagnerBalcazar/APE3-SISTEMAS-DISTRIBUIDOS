package edu.ape3.microservicio1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class ParkingSimulationService {
    private static final long PARKING_TIME_MILLIS = 200;

    // Service Layer: encapsula la coordinacion y evita que el controlador conozca los hilos.
    public SimulationResult simulate(int capacity, int vehicleCount) {
        Semaphore spaces = new Semaphore(capacity, true);
        AtomicInteger vehiclesInside = new AtomicInteger();
        AtomicInteger maximumInside = new AtomicInteger();
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(vehicleCount);
        List<Future<VehicleResult>> futures = new ArrayList<>();

        for (int id = 1; id <= vehicleCount; id++) {
            futures.add(executor.submit(new VehicleTask(
                    id, spaces, vehiclesInside, maximumInside, events)));
        }

        List<VehicleResult> vehicles = new ArrayList<>();
        for (Future<VehicleResult> future : futures) {
            try {
                vehicles.add(future.get());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("La simulacion fue interrumpida", exception);
            } catch (ExecutionException exception) {
                throw new IllegalStateException("No se pudo ejecutar un vehiculo", exception.getCause());
            }
        }
        executor.shutdown();

        return new SimulationResult(
                capacity,
                vehicleCount,
                maximumInside.get(),
                vehicleCount - capacity,
                spaces.availablePermits(),
                vehicles,
                events,
                Map.of(
                        "semaphoreInitializedWithOne", "Solo un vehiculo puede permanecer dentro; los otros esperan.",
                        "semaphoreInitializedWithTen", "Los diez vehiculos pueden ingresar, superando la capacidad del estacionamiento."));
    }

    private static final class VehicleTask implements Callable<VehicleResult> {
        private final int id;
        private final Semaphore spaces;
        private final AtomicInteger vehiclesInside;
        private final AtomicInteger maximumInside;
        private final List<String> events;

        private VehicleTask(int id, Semaphore spaces, AtomicInteger vehiclesInside,
                            AtomicInteger maximumInside, List<String> events) {
            this.id = id;
            this.spaces = spaces;
            this.vehiclesInside = vehiclesInside;
            this.maximumInside = maximumInside;
            this.events = events;
        }

        @Override
        public VehicleResult call() throws InterruptedException {
            events.add("Vehiculo " + id + " llega y solicita un espacio");
            long waitStart = System.nanoTime();
            spaces.acquire();
            long waitedMillis = (System.nanoTime() - waitStart) / 1_000_000;
            int inside = vehiclesInside.incrementAndGet();
            maximumInside.accumulateAndGet(inside, Math::max);
            events.add("Vehiculo " + id + " ingresa (ocupados: " + inside + ")");

            try {
                Thread.sleep(PARKING_TIME_MILLIS);
                return new VehicleResult(id, waitedMillis, true);
            } finally {
                int remaining = vehiclesInside.decrementAndGet();
                spaces.release();
                events.add("Vehiculo " + id + " sale y libera un espacio (ocupados: " + remaining + ")");
            }
        }
    }

    public record VehicleResult(int id, long waitedMillis, boolean completed) { }

    public record SimulationResult(
            int capacity,
            int totalVehicles,
            int maximumVehiclesInside,
            int vehiclesThatHadToWait,
            int availablePermitsAtEnd,
            List<VehicleResult> vehicles,
            List<String> events,
            Map<String, String> analysis) { }
}
