package cl.duoc.nexoramarket.ms_kafka_admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.nexoramarket.ms_kafka_admin.config.SecurityConfig;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.BrokerInfo;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.ClusterInfo;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.OperacionResponse;
import cl.duoc.nexoramarket.ms_kafka_admin.exception.TopicoProtegidoException;
import cl.duoc.nexoramarket.ms_kafka_admin.service.KafkaAdminService;

@WebMvcTest(controllers = KafkaAdminController.class)
@Import(SecurityConfig.class)
class KafkaAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private KafkaAdminService adminService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void sinTokenResponde401() throws Exception {
		mockMvc.perform(get("/api/kafka-admin/cluster"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void clusterConTokenResponde200() throws Exception {
		when(adminService.cluster()).thenReturn(new ClusterInfo(
				List.of(new BrokerInfo(1, "kafka1", 9092)),
				new BrokerInfo(1, "kafka1", 9092)));

		mockMvc.perform(get("/api/kafka-admin/cluster").with(jwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.brokers[0].id").value(1))
				.andExpect(jsonPath("$.brokers[0].host").value("kafka1"))
				.andExpect(jsonPath("$.controller.id").value(1));
	}

	@Test
	void crearTopicoResponde201() throws Exception {
		when(adminService.crearTopico(any()))
				.thenReturn(new OperacionResponse("nexora.demo.eventos", "Tópico creado"));

		mockMvc.perform(post("/api/kafka-admin/topicos")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nombre":"nexora.demo.eventos","particiones":3,"replicacion":3}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nombre").value("nexora.demo.eventos"));
	}

	@Test
	void crearTopicoConNombreInvalidoResponde400() throws Exception {
		mockMvc.perform(post("/api/kafka-admin/topicos")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nombre":"nombre con espacios","particiones":3,"replicacion":3}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void eliminarTopicoProtegidoSinForzarResponde403() throws Exception {
		doThrow(new TopicoProtegidoException("tópico del negocio"))
				.when(adminService).eliminarTopico("nexora.pedidos.eventos", false);

		mockMvc.perform(delete("/api/kafka-admin/topicos/nexora.pedidos.eventos").with(jwt()))
				.andExpect(status().isForbidden());
	}

	@Test
	void eliminarTopicoConForzarResponde204() throws Exception {
		mockMvc.perform(delete("/api/kafka-admin/topicos/nexora.pedidos.eventos")
				.param("forzar", "true")
				.with(jwt()))
				.andExpect(status().isNoContent());
	}
}
