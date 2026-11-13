package cl.duoc.nexoramarket.ms_kafka_admin.dto;

public record TopicoResumen(
		String nombre,
		int particiones,
		int replicas,
		int isr,
		long retencionMs) {
}
