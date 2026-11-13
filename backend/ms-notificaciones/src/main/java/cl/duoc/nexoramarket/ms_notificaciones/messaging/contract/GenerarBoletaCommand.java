package cl.duoc.nexoramarket.ms_notificaciones.messaging.contract;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Comando publicado por ms-pagos en el exchange DIRECT {@code nexora.documentos.direct}
 * (routing key {@code boleta.generar}) cuando un pago queda APROBADO.
 */
public record GenerarBoletaCommand(
		@NotBlank String comandoId,
		@NotNull Long pagoId,
		@NotNull Long pedidoId,
		@NotBlank String clienteId,
		@NotNull @Positive Double monto,
		@NotBlank String metodoPago,
		LocalDateTime fechaPago) {
}
