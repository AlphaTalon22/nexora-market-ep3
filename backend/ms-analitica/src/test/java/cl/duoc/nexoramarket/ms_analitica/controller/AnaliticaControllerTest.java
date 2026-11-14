package cl.duoc.nexoramarket.ms_analitica.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.nexoramarket.ms_analitica.config.SecurityConfig;
import cl.duoc.nexoramarket.ms_analitica.dto.AlertaStock;
import cl.duoc.nexoramarket.ms_analitica.dto.ResumenAnalitica;
import cl.duoc.nexoramarket.ms_analitica.service.InventarioAnalyticsService;
import cl.duoc.nexoramarket.ms_analitica.service.PedidoAnalyticsService;

@WebMvcTest(controllers = AnaliticaController.class)
@Import(SecurityConfig.class)
class AnaliticaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PedidoAnalyticsService pedidoAnalyticsService;

	@MockitoBean
	private InventarioAnalyticsService inventarioAnalyticsService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void sinTokenResponde401() throws Exception {
		mockMvc.perform(get("/api/analitica/resumen")).andExpect(status().isUnauthorized());
	}

	@Test
	void conTokenElResumenResponde200() throws Exception {
		when(pedidoAnalyticsService.resumen()).thenReturn(new ResumenAnalitica(12, 8, 960000.0));

		mockMvc.perform(get("/api/analitica/resumen").with(jwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pedidos").value(12))
				.andExpect(jsonPath("$.ventas").value(8))
				.andExpect(jsonPath("$.ingreso").value(960000.0));
	}

	@Test
	void conTokenLasAlertasResponden200() throws Exception {
		when(inventarioAnalyticsService.alertas())
				.thenReturn(List.of(new AlertaStock(7L, "Mouse", 2)));

		mockMvc.perform(get("/api/analitica/alertas").with(jwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].productoId").value(7))
				.andExpect(jsonPath("$[0].nombre").value("Mouse"))
				.andExpect(jsonPath("$[0].stock").value(2));
	}
}
