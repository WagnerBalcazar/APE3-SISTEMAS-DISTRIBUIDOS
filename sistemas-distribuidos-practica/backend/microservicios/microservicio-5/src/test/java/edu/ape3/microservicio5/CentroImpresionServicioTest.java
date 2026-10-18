package edu.ape3.microservicio5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ape3.microservicio5.modelo.EstadoCentro;
import edu.ape3.microservicio5.modelo.dto.RespuestaEtapa;
import edu.ape3.microservicio5.servicio.CentroImpresionServicio;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class CentroImpresionServicioTest {

    @Test
    void estadoInicialSoloHabilitaDiseno() {
        CentroImpresionServicio centro = new CentroImpresionServicio();
        EstadoCentro estado = centro.estado();
        assertEquals("NINGUNA", estado.etapaActual());
        assertEquals(0, estado.documentosDisenados());
        assertEquals(0, estado.documentosRevisados());
        assertEquals(0, estado.documentosImpresos());
        assertEquals(3, estado.ciclosTotales());
        assertFalse(estado.completado());
    }

    @Test
    void revisionEsperaHastaQueDisenoHabilita() throws Exception {
        CentroImpresionServicio centro = new CentroImpresionServicio();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<RespuestaEtapa> revision = pool.submit(() -> centro.revisar(null, 0));
            assertThrows(TimeoutException.class, () -> revision.get(300, TimeUnit.MILLISECONDS));

            centro.disenar(null, 0);
            RespuestaEtapa respuesta = revision.get(2, TimeUnit.SECONDS);
            assertEquals("REVISION", respuesta.etapa());
            assertEquals(1, respuesta.documentosRevisados());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void impresionEsperaHastaQueRevisionHabilita() throws Exception {
        CentroImpresionServicio centro = new CentroImpresionServicio();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<RespuestaEtapa> impresion = pool.submit(() -> centro.imprimir(null, 0));
            assertThrows(TimeoutException.class, () -> impresion.get(300, TimeUnit.MILLISECONDS));

            centro.disenar(null, 0);
            assertThrows(TimeoutException.class, () -> impresion.get(300, TimeUnit.MILLISECONDS));

            centro.revisar(null, 0);
            RespuestaEtapa respuesta = impresion.get(2, TimeUnit.SECONDS);
            assertEquals("IMPRESION", respuesta.etapa());
            assertEquals(1, respuesta.documentosImpresos());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void cicloCompletoDeTresDocumentos() {
        CentroImpresionServicio centro = new CentroImpresionServicio();
        for (int i = 0; i < 3; i++) {
            RespuestaEtapa diseno = centro.disenar(null, 0);
            RespuestaEtapa revision = centro.revisar(null, 0);
            RespuestaEtapa impresion = centro.imprimir(null, 0);
            assertEquals(diseno.documento(), revision.documento());
            assertEquals(revision.documento(), impresion.documento());
        }
        EstadoCentro estado = centro.estado();
        assertEquals(3, estado.documentosDisenados());
        assertEquals(3, estado.documentosRevisados());
        assertEquals(3, estado.documentosImpresos());
        assertTrue(estado.completado());
    }

    @Test
    void disenoAdicionalNoSuperaElCiclo() {
        CentroImpresionServicio centro = new CentroImpresionServicio();
        for (int i = 0; i < 3; i++) {
            centro.disenar(null, 0);
            centro.revisar(null, 0);
            centro.imprimir(null, 0);
        }
        RespuestaEtapa extra = centro.disenar(null, 0);
        assertEquals(3, extra.documentosDisenados());
        assertEquals(3, centro.estado().documentosDisenados());
    }

    @Test
    void lasTresEtapasCoordinanEntreHilos() throws Exception {
        CentroImpresionServicio centro = new CentroImpresionServicio();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        List<Future<?>> tareas = new ArrayList<>();
        tareas.add(pool.submit(() -> {
            for (int i = 0; i < 3; i++) {
                centro.disenar(null, 0);
            }
        }));
        tareas.add(pool.submit(() -> {
            for (int i = 0; i < 3; i++) {
                centro.revisar(null, 0);
            }
        }));
        tareas.add(pool.submit(() -> {
            for (int i = 0; i < 3; i++) {
                centro.imprimir(null, 0);
            }
        }));
        for (Future<?> tarea : tareas) {
            tarea.get(10, TimeUnit.SECONDS);
        }
        pool.shutdownNow();

        EstadoCentro estado = centro.estado();
        assertEquals(3, estado.documentosDisenados());
        assertEquals(3, estado.documentosRevisados());
        assertEquals(3, estado.documentosImpresos());
        assertTrue(estado.completado());
    }

    @Test
    void resetReiniciaSemaforosYContadores() {
        CentroImpresionServicio centro = new CentroImpresionServicio();
        for (int i = 0; i < 3; i++) {
            centro.disenar(null, 0);
            centro.revisar(null, 0);
            centro.imprimir(null, 0);
        }
        EstadoCentro reiniciado = centro.reset();
        assertEquals(0, reiniciado.documentosDisenados());
        assertEquals(0, reiniciado.documentosRevisados());
        assertEquals(0, reiniciado.documentosImpresos());
        assertEquals("NINGUNA", reiniciado.etapaActual());
        assertFalse(reiniciado.completado());

        centro.disenar(null, 0);
        assertEquals(1, centro.estado().documentosDisenados());
    }
}
