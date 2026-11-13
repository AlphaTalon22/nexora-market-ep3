package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import java.util.List;

public record MiembroGrupo(
		String miembroId,
		String clientId,
		String host,
		List<String> particiones) {
}
