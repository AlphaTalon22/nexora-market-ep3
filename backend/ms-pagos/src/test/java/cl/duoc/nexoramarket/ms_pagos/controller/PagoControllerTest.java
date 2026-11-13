package cl.duoc.nexoramarket.ms_pagos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.nexoramarket.ms_pagos.config.SecurityConfig;
import cl.duoc.nexoramarket.ms_pagos.model.Pago;
import cl.duoc.nexoramarket.ms_pagos.service.PagoService;

@WebMvcTest(controllers = PagoController.class)
@Import(SecurityConfig.class)
class PagoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PagoService pagoService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	private static final String BODY = """
			{"pedidoId":10,"metodoPago":"TARJETA_CREDITO","ultimos4":"1234"}
			""";

	@Test
	void sinTokenResponde401() throws Exception {
		mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void conTokenResponde201() throws Exception {
		Pago pago = new Pago(5L, 10L, "oid-123", 450000.0, "TARJETA_CREDITO", "1234", "APROBADO",
				LocalDateTime.now());
		when(pagoService.procesar(any(), eq("oid-123"))).thenReturn(pago);

		mockMvc.perform(post("/api/pagos")
				.with(jwt().jwt(jwt -> jwt.claim("oid", "oid-123")))
				.contentType(MediaType.APPLICATION_JSON)
				.content(BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.pedidoId").value(10))
				.andExpect(jsonPath("$.monto").value(450000.0))
				.andExpect(jsonPath("$.ultimos4").value("1234"))
				.andExpect(jsonPath("$.estado").value("APROBADO"));
	}
}
