package cl.duoc.nexoramarket.ms_analitica.exception;

/**
 * Evento de Kafka con datos inválidos (Bean Validation).
 * Es un error no recuperable: el DefaultErrorHandler lo manda directo al DLT.
 */
public class EventoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EventoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
