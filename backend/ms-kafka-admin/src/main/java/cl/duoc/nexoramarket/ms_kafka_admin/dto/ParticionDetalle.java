package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import java.util.List;

public record ParticionDetalle(
		int particion,
		Integer lider,
		List<Integer> replicas,
		List<Integer> isr,
		long offsetInicio,
		long offsetFin) {
}
