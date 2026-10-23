package br.senac.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveListarCursosVazio() throws Exception {
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isOk());
    }

    @Test
    void deveCriarCurso() throws Exception {
        String json = """
                {"nome":"Docker na prática","descricao":"Containers","cargaHoraria":40}
                """;

        mockMvc.perform(post("/api/cursos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Docker na prática"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveRejeitarCursoInvalido() throws Exception {
        String json = """
                {"nome":"","cargaHoraria":0}
                """;

        mockMvc.perform(post("/api/cursos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Erro de validação"));
    }

    @Test
    void deveRetornar404ParaCursoInexistente() throws Exception {
        mockMvc.perform(get("/api/cursos/9999"))
                .andExpect(status().isNotFound());
    }
}
