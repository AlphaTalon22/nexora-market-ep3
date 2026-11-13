package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;

public record ActualizarConfiguracionRequest(
		@NotEmpty(message = "Debe indicar al menos una configuración")
		Map<String, String> configuracion) {
}
