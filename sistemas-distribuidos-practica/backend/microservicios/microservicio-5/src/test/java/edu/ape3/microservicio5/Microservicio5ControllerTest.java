package edu.ape3.microservicio5;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ape3.microservicio5.servicio.CentroImpresionServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class Microservicio5ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CentroImpresionServicio centro;

    @BeforeEach
    void limpiar() {
        centro.reset();
        centro.setCiclos(3);
    }

    @Test
    void healthResponde() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("microservicio-5"))
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void apiBaseResponde() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("microservicio-5"));
    }

    @Test
    void estadoInicialEstaVacio() throws Exception {
        mockMvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etapaActual").value("NINGUNA"))
                .andExpect(jsonPath("$.documentosDisenados").value(0))
                .andExpect(jsonPath("$.completado").value(false));
    }

    @Test
    void cicloDeUnDocumentoPorApi() throws Exception {
        mockMvc.perform(post("/api/diseno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"doc-1\",\"demoraMs\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento").value("doc-1"))
                .andExpect(jsonPath("$.etapa").value("DISENO"))
                .andExpect(jsonPath("$.documentosDisenados").value(1));

        mockMvc.perform(post("/api/revision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"demoraMs\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento").value("doc-1"))
                .andExpect(jsonPath("$.etapa").value("REVISION"))
                .andExpect(jsonPath("$.documentosRevisados").value(1));

        mockMvc.perform(post("/api/impresion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"demoraMs\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento").value("doc-1"))
                .andExpect(jsonPath("$.etapa").value("IMPRESION"))
                .andExpect(jsonPath("$.documentosImpresos").value(1));
    }

    @Test
    void ciclosConfiguraLaCantidadDeDocumentos() throws Exception {
        mockMvc.perform(post("/api/ciclos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ciclos\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ciclosTotales").value(5));
    }

    @Test
    void resetReiniciaElEstado() throws Exception {
        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etapaActual").value("NINGUNA"))
                .andExpect(jsonPath("$.documentosDisenados").value(0))
                .andExpect(jsonPath("$.documentosRevisados").value(0))
                .andExpect(jsonPath("$.documentosImpresos").value(0));
    }
}
