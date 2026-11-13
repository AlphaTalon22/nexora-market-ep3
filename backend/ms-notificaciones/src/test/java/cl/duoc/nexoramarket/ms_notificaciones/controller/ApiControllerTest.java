package cl.duoc.nexoramarket.ms_notificaciones.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.nexoramarket.ms_notificaciones.boleta.BoletaService;
import cl.duoc.nexoramarket.ms_notificaciones.config.SecurityConfig;
import cl.duoc.nexoramarket.ms_notificaciones.model.Boleta;
import cl.duoc.nexoramarket.ms_notificaciones.model.Notificacion;
import cl.duoc.nexoramarket.ms_notificaciones.notificacion.NotificacionService;

@WebMvcTest(controllers = { NotificacionController.class, BoletaController.class })
@Import(SecurityConfig.class)
class ApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificacionService notificacionService;

	@MockitoBean
	private BoletaService boletaService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void sinTokenNotificacionesResponde401() throws Exception {
		mockMvc.perform(get("/api/notificaciones"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void conTokenNotificacionesResponde200() throws Exception {
		when(notificacionService.listarPorCliente("oid-123")).thenReturn(List.of(
				new Notificacion(1L, "oid-123", "PAGO", "Tu pago fue APROBADO.", LocalDateTime.now(), false)));

		mockMvc.perform(get("/api/notificaciones")
				.with(jwt().jwt(jwt -> jwt.claim("oid", "oid-123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tipo").value("PAGO"))
				.andExpect(jsonPath("$[0].mensaje").value("Tu pago fue APROBADO."));
	}

	@Test
	void sinTokenBoletasResponde401() throws Exception {
		mockMvc.perform(get("/api/boletas"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void conTokenBoletasResponde200() throws Exception {
		when(boletaService.listarPorCliente("oid-123")).thenReturn(List.of(
				new Boleta(1L, 5L, 10L, "oid-123", 450000.0, "TARJETA_DEBITO", LocalDateTime.now())));

		mockMvc.perform(get("/api/boletas")
				.with(jwt().jwt(jwt -> jwt.claim("oid", "oid-123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].pedidoId").value(10))
				.andExpect(jsonPath("$[0].monto").value(450000.0))
				.andExpect(jsonPath("$[0].metodoPago").value("TARJETA_DEBITO"));
	}
}
