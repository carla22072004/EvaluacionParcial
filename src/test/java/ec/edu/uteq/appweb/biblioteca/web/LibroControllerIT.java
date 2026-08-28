package ec.edu.uteq.appweb.biblioteca.web;

import ec.edu.uteq.appweb.biblioteca.BaseIntegracionTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ============================================================================
 * TODO-U4-5: PRUEBAS DE INTEGRACION (minimo 10 en total en el proyecto)
 * ============================================================================
 *
 * Quite la anotacion @Disabled a medida que implemente cada caso. La Guia PE-U4
 * exige un minimo de diez pruebas de feature o integracion pasando sin fallos,
 * y pide la captura del resultado de la ejecucion dentro del informe.
 *
 * Casos minimos sugeridos, alineados con la rubrica:
 *   1. Login con credenciales correctas devuelve 200 y un token.
 *   2. Login con credenciales invalidas devuelve 401.
 *   3. GET /api/v1/libros sin token devuelve 401.
 *   4. GET /api/v1/libros con rol LECTOR devuelve 200 y trae meta.
 *   5. POST /api/v1/libros con rol LECTOR devuelve 403.
 *   6. POST /api/v1/libros con rol ADMIN y cuerpo valido devuelve 201 con Location.
 *   7. GET /api/v1/libros/{id} inexistente devuelve 404 con ProblemDetail.
 *   8. POST /api/v1/libros con cuerpo invalido devuelve 400 y el arreglo errors poblado.
 *   9. POST /api/v1/prestamos a un socio con tres prestamos activos devuelve 409.
 *  10. Toda respuesta exitosa trae el envoltorio {success, data, message, errors, meta}.
 *
 * Ejecute con:  mvn test
 */
class LibroControllerIT extends BaseIntegracionTest {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @org.springframework.beans.factory.annotation.Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    private ec.edu.uteq.appweb.biblioteca.security.JwtService jwtService;

    @Test
    @DisplayName("GET /api/v1/libros responde 200 y el cuerpo trae las cinco claves del envoltorio, con meta.page y meta.size correctos")
    void listarLibrosDevuelveEnvoltorio() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/libros"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.errors").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.meta").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.meta.page").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.meta.size").exists());
    }

    @Test
    @DisplayName("GET /api/v1/libros/999999 responde 404 y el cuerpo trae title, status y detail del formato Problem Details")
    void buscarLibroNoExistenteDevuelve404() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/libros/999999"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("POST /api/v1/libros con titulo vacio responde 400 y el arreglo errors no esta vacio")
    void crearLibroInvalidoDevuelve400() throws Exception {
        ec.edu.uteq.appweb.biblioteca.domain.Usuario admin = new ec.edu.uteq.appweb.biblioteca.domain.Usuario();
        admin.setUsername("admin");
        admin.setRol(ec.edu.uteq.appweb.biblioteca.domain.Rol.ADMIN);
        String token = jwtService.generar(admin);

        ec.edu.uteq.appweb.biblioteca.web.dto.LibroRequest request = new ec.edu.uteq.appweb.biblioteca.web.dto.LibroRequest(
                "978-0134685991", "", 2018, 5, 1L, 1L, 1L
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/libros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.errors").isNotEmpty());
    }
}
