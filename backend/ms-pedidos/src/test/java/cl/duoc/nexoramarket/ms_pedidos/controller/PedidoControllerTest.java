package cl.duoc.nexoramarket.ms_pedidos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.nexoramarket.ms_pedidos.config.SecurityConfig;
import cl.duoc.nexoramarket.ms_pedidos.dto.CrearPedidoRequest;
import cl.duoc.nexoramarket.ms_pedidos.exception.ProductoNoDisponibleException;
import cl.duoc.nexoramarket.ms_pedidos.model.ItemPedido;
import cl.duoc.nexoramarket.ms_pedidos.model.Pedido;
import cl.duoc.nexoramarket.ms_pedidos.service.PedidoService;

@WebMvcTest(controllers = PedidoController.class)
@Import(SecurityConfig.class)
class PedidoControllerTest {

    private static final String JSON_VALIDO = """
            {"items":[{"productoId":1,"cantidad":2}]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Pedido pedido() {
        Pedido pedido = new Pedido(50L, "user", LocalDateTime.now(), 39980.0, Pedido.PENDIENTE);
        pedido.agregarItem(new ItemPedido(1L, "Mouse", 19990.0, 2));
        return pedido;
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
    }

    @Test
    void listarDevuelveLosPedidosDelClienteDelToken() throws Exception {
        when(pedidoService.listar("user")).thenReturn(List.of(pedido()));

        mockMvc.perform(get("/api/pedidos").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].items[0].nombreProducto").value("Mouse"));
    }

    @Test
    void crearConTokenResponde201ConElTotalDelBackend() throws Exception {
        when(pedidoService.crear(eq("user"), any(CrearPedidoRequest.class))).thenReturn(pedido());

        mockMvc.perform(post("/api/pedidos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(39980.0))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.clienteId").value("user"));
    }

    @Test
    void pedidoSinItemsResponde400() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void productoNoDisponibleResponde409() throws Exception {
        when(pedidoService.crear(eq("user"), any(CrearPedidoRequest.class)))
                .thenThrow(new ProductoNoDisponibleException("Stock insuficiente de Mouse"));

        mockMvc.perform(post("/api/pedidos").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Stock insuficiente de Mouse"));
    }
}
