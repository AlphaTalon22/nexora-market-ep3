package cl.duoc.nexoramarket.ms_kafka_admin.dto;

public record LagParticion(
		String topico,
		int particion,
		long offsetConsumido,
		long offsetFinal,
		long lag) {
}
