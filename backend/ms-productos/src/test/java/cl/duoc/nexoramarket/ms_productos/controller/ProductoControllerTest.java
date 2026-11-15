package cl.duoc.nexoramarket.ms_productos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.nexoramarket.ms_productos.config.SecurityConfig;
import cl.duoc.nexoramarket.ms_productos.model.Producto;
import cl.duoc.nexoramarket.ms_productos.service.ProductoService;

@WebMvcTest(controllers = ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerTest {

    private static final String JSON_VALIDO = """
            {"nombre":"Mouse","descripcion":"Gamer","precio":19990,"stock":10,
             "categoria":"Computación","imagen":"/img/productos/mouse.svg"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Producto mouse() {
        return new Producto(1L, "Mouse", "Gamer", 19990.0, 10, "Computación", "/img/productos/mouse.svg", 5);
    }

    @Test
    void elCatalogoEsPublico() throws Exception {
        when(productoService.listar()).thenReturn(List.of(mouse()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Mouse"))
                .andExpect(jsonPath("$[0].imagen").value("/img/productos/mouse.svg"));
    }

    @Test
    void productoInexistenteResponde404() throws Exception {
        when(productoService.buscar(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/productos/99")).andExpect(status().isNotFound());
    }

    @Test
    void crearSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearConTokenResponde201() throws Exception {
        when(productoService.crear(any(Producto.class))).thenReturn(mouse());

        mockMvc.perform(post("/api/productos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void precioNegativoResponde400() throws Exception {
        String invalido = """
                {"nombre":"Mouse","precio":-1,"stock":10}
                """;
        mockMvc.perform(post("/api/productos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }
}
