package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import java.util.Map;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CrearTopicoRequest(
		@NotBlank(message = "El nombre es obligatorio")
		@Pattern(regexp = ReglasNombres.NOMBRE_TOPICO, message = ReglasNombres.NOMBRE_TOPICO_MENSAJE)
		String nombre,

		@NotNull(message = "El número de particiones es obligatorio")
		@Min(value = 1, message = "Debe haber al menos 1 partición")
		Integer particiones,

		@NotNull(message = "El factor de réplica es obligatorio")
		@Min(value = 1, message = "Debe haber al menos 1 réplica")
		Integer replicacion,

		Map<String, String> configuracion) {
}
