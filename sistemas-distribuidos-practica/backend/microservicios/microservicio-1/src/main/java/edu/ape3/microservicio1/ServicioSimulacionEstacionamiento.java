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
public class ServicioSimulacionEstacionamiento {
    private static final long TIEMPO_ESTACIONAMIENTO_MILLIS = 200;

    // Service Layer: encapsula la coordinacion y evita que el controlador conozca los hilos.
    public ResultadoSimulacion simular(int capacidad, int cantidadVehiculos) {
        Semaphore espacios = new Semaphore(capacidad, true);
        AtomicInteger vehiculosDentro = new AtomicInteger();
        AtomicInteger maximoDentro = new AtomicInteger();
        List<String> eventos = Collections.synchronizedList(new ArrayList<>());
        ExecutorService ejecutor = Executors.newFixedThreadPool(cantidadVehiculos);
        List<Future<ResultadoVehiculo>> futuros = new ArrayList<>();

        for (int id = 1; id <= cantidadVehiculos; id++) {
            futuros.add(ejecutor.submit(new TareaVehiculo(
                    id, espacios, vehiculosDentro, maximoDentro, eventos)));
        }

        List<ResultadoVehiculo> vehiculos = new ArrayList<>();
        for (Future<ResultadoVehiculo> futuro : futuros) {
            try {
                vehiculos.add(futuro.get());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("La simulacion fue interrumpida", exception);
            } catch (ExecutionException exception) {
                throw new IllegalStateException("No se pudo ejecutar un vehiculo", exception.getCause());
            }
        }
        ejecutor.shutdown();

        return new ResultadoSimulacion(
                capacidad,
                cantidadVehiculos,
                maximoDentro.get(),
                cantidadVehiculos - capacidad,
                espacios.availablePermits(),
                vehiculos,
                eventos,
                Map.of(
                        "semaphoreInitializedWithOne", "Solo un vehiculo puede permanecer dentro; los otros esperan.",
                        "semaphoreInitializedWithTen", "Los diez vehiculos pueden ingresar, superando la capacidad del estacionamiento."));
    }

    private static final class TareaVehiculo implements Callable<ResultadoVehiculo> {
        private final int id;
        private final Semaphore espacios;
        private final AtomicInteger vehiculosDentro;
        private final AtomicInteger maximoDentro;
        private final List<String> eventos;

        private TareaVehiculo(int id, Semaphore espacios, AtomicInteger vehiculosDentro,
                            AtomicInteger maximoDentro, List<String> eventos) {
            this.id = id;
            this.espacios = espacios;
            this.vehiculosDentro = vehiculosDentro;
            this.maximoDentro = maximoDentro;
            this.eventos = eventos;
        }

        @Override
        public ResultadoVehiculo call() throws InterruptedException {
            eventos.add("Vehiculo " + id + " llega y solicita un espacio");
            long inicioEspera = System.nanoTime();
            espacios.acquire();
            long milisegundosEsperados = (System.nanoTime() - inicioEspera) / 1_000_000;
            int ocupados = vehiculosDentro.incrementAndGet();
            maximoDentro.accumulateAndGet(ocupados, Math::max);
            eventos.add("Vehiculo " + id + " ingresa (ocupados: " + ocupados + ")");

            try {
                Thread.sleep(TIEMPO_ESTACIONAMIENTO_MILLIS);
                return new ResultadoVehiculo(id, milisegundosEsperados, true);
            } finally {
                int restantes = vehiculosDentro.decrementAndGet();
                espacios.release();
                eventos.add("Vehiculo " + id + " sale y libera un espacio (ocupados: " + restantes + ")");
            }
        }
    }

    public record ResultadoVehiculo(int id, long milisegundosEsperados, boolean completado) { }

    public record ResultadoSimulacion(
            int capacidad,
            int totalVehiculos,
            int maximoVehiculosDentro,
            int vehiculosQueEsperaron,
            int permisosDisponiblesAlFinal,
            List<ResultadoVehiculo> vehiculos,
            List<String> eventos,
            Map<String, String> analisis) { }
}
