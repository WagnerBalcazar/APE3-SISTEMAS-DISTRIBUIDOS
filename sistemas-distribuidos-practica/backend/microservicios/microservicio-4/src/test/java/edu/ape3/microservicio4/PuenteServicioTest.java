package edu.ape3.microservicio4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ape3.microservicio4.modelo.Direccion;
import edu.ape3.microservicio4.modelo.EstadoPuente;
import edu.ape3.microservicio4.servicio.PuenteServicio;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class PuenteServicioTest {

    @Test
    void unVehiculoSurEsperaMientrasHayVehiculosNorte() throws Exception {
        PuenteServicio puente = new PuenteServicio();
        puente.entrar(Direccion.NORTE);
        assertEquals("NORTE", puente.estado().direccion());
        assertEquals(1, puente.estado().vehiculosNorte());

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<Long> sur = pool.submit(() -> puente.entrar(Direccion.SUR));
            assertThrows(TimeoutException.class, () -> sur.get(300, TimeUnit.MILLISECONDS));

            puente.salir(Direccion.NORTE);
            sur.get(2, TimeUnit.SECONDS);
            assertEquals("SUR", puente.estado().direccion());
            assertEquals(1, puente.estado().vehiculosSur());
            assertEquals(0, puente.estado().vehiculosNorte());

            puente.salir(Direccion.SUR);
            assertEquals("NINGUNA", puente.estado().direccion());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void variosVehiculosDeLaMismaDireccionCruzanALaVez() throws Exception {
        PuenteServicio puente = new PuenteServicio();
        ExecutorService pool = Executors.newFixedThreadPool(5);
        try {
            pool.submit(() -> puente.entrar(Direccion.NORTE)).get(1, TimeUnit.SECONDS);
            pool.submit(() -> puente.entrar(Direccion.NORTE)).get(1, TimeUnit.SECONDS);
            assertEquals(2, puente.estado().vehiculosNorte());
            puente.salir(Direccion.NORTE);
            puente.salir(Direccion.NORTE);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void concurrenciaNoViolaLaExclusividadDeDireccion() throws Exception {
        PuenteServicio puente = new PuenteServicio();
        int norte = 40;
        int sur = 40;
        AtomicBoolean corriendo = new AtomicBoolean(true);
        AtomicBoolean violacion = new AtomicBoolean(false);

        Thread monitor = new Thread(() -> {
            while (corriendo.get()) {
                EstadoPuente e = puente.estado();
                if (e.vehiculosNorte() > 0 && e.vehiculosSur() > 0) {
                    violacion.set(true);
                }
                if (e.vehiculosNorte() < 0 || e.vehiculosSur() < 0) {
                    violacion.set(true);
                }
            }
        });
        monitor.start();

        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<Future<?>> tareas = new ArrayList<>();
        for (int i = 0; i < norte; i++) {
            tareas.add(pool.submit(() -> cruzar(puente, Direccion.NORTE)));
        }
        for (int i = 0; i < sur; i++) {
            tareas.add(pool.submit(() -> cruzar(puente, Direccion.SUR)));
        }
        for (Future<?> tarea : tareas) {
            tarea.get(15, TimeUnit.SECONDS);
        }

        corriendo.set(false);
        monitor.join(1000);
        pool.shutdownNow();

        assertFalse(violacion.get(), "Nunca deben coexistir vehículos del NORTE y del SUR");
        assertEquals(0, puente.estado().vehiculosNorte());
        assertEquals(0, puente.estado().vehiculosSur());
        assertEquals("NINGUNA", puente.estado().direccion());
    }

    @Test
    void equidadEvitaLaInanicionDeLaDireccionContraria() throws Exception {
        PuenteServicio puente = new PuenteServicio();
        puente.setEquidad(true);

        AtomicBoolean seguir = new AtomicBoolean(true);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<?>> tormenta = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tormenta.add(pool.submit(() -> {
                while (seguir.get()) {
                    cruzar(puente, Direccion.NORTE);
                }
            }));
        }

        try {
            Thread.sleep(200);
            long inicio = System.currentTimeMillis();
            Future<?> sur = pool.submit(() -> cruzar(puente, Direccion.SUR));
            sur.get(3, TimeUnit.SECONDS);
            long transcurrido = System.currentTimeMillis() - inicio;
            assertTrue(transcurrido < 3000, "El vehículo SUR debió entrar en tiempo acotado");
        } finally {
            seguir.set(false);
            for (Future<?> f : tormenta) {
                f.cancel(true);
            }
            pool.shutdownNow();
        }
    }

    private void cruzar(PuenteServicio puente, Direccion direccion) {
        puente.entrar(direccion);
        puente.cruzar(2);
        puente.salir(direccion);
    }
}
