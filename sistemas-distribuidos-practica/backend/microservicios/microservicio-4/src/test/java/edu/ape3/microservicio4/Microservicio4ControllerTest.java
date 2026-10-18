package edu.ape3.microservicio4;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ape3.microservicio4.servicio.PuenteServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class Microservicio4ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PuenteServicio puente;

    @BeforeEach
    void limpiar() {
        puente.reset();
    }

    @Test
    void healthResponde() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("microservicio-4"))
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void apiBaseResponde() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("microservicio-4"));
    }

    @Test
    void estadoInicialEsNinguna() throws Exception {
        mockMvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("NINGUNA"))
                .andExpect(jsonPath("$.vehiculosNorte").value(0))
                .andExpect(jsonPath("$.vehiculosSur").value(0));
    }

    @Test
    void unVehiculoCruzaHaciaElNorte() throws Exception {
        mockMvc.perform(post("/api/norte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"auto-1\",\"demoraMs\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehiculo").value("auto-1"))
                .andExpect(jsonPath("$.direccion").value("NORTE"))
                .andExpect(jsonPath("$.mensaje").value("Vehículo auto-1 cruza hacia el NORTE"));
    }

    @Test
    void unVehiculoCruzaHaciaElSur() throws Exception {
        mockMvc.perform(post("/api/sur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"auto-2\",\"demoraMs\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("SUR"))
                .andExpect(jsonPath("$.mensaje").value("Vehículo auto-2 cruza hacia el SUR"));
    }

    @Test
    void equidadSeActivaYDesactiva() throws Exception {
        mockMvc.perform(post("/api/equidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activa\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equidadActiva").value(true));

        mockMvc.perform(post("/api/equidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activa\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equidadActiva").value(false));
    }

    @Test
    void resetReiniciaElEstado() throws Exception {
        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("NINGUNA"))
                .andExpect(jsonPath("$.vehiculosNorte").value(0))
                .andExpect(jsonPath("$.vehiculosSur").value(0));
    }
}
