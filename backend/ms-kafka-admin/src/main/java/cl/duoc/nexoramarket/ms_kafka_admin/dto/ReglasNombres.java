package cl.duoc.nexoramarket.ms_kafka_admin.dto;

public final class ReglasNombres {

	public static final String NOMBRE_TOPICO = "[a-zA-Z0-9._-]+";

	public static final String NOMBRE_TOPICO_MENSAJE =
			"El nombre solo admite letras, números, punto, guion y guion bajo";

	private ReglasNombres() {
	}
}
