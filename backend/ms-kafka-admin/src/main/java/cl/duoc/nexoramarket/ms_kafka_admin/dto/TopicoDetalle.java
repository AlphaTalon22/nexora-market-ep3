package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import java.util.List;
import java.util.Map;

public record TopicoDetalle(
		String nombre,
		List<ParticionDetalle> particiones,
		Map<String, String> configuracion) {
}
