package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AumentarParticionesRequest(
		@NotNull(message = "El número de particiones es obligatorio")
		@Min(value = 1, message = "Debe haber al menos 1 partición")
		Integer particiones) {
}
