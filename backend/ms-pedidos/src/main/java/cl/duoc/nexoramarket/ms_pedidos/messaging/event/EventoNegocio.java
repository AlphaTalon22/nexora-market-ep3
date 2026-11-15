package cl.duoc.nexoramarket.ms_pedidos.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Evento de dominio que viaja como JSON por {@code nexora.eventos.topic} (RabbitMQ).
 * Mismo contrato que usan ms-pagos y ms-notificaciones (clienteId = oid de Entra ID).
 */
public record EventoNegocio(
		@NotBlank String eventoId,
		@NotBlank String tipo,
		String origen,
		LocalDateTime ocurridoEn,
		@NotNull Long pedidoId,
		Long pagoId,
		String clienteId,
		Double monto,
		String estado,
		String metodoPago) {

	/** Construye el evento de RabbitMQ que se publica cuando se crea un pedido. */
	public static EventoNegocio pedidoCreado(String tipo, PedidoEvento pedido) {
		return new EventoNegocio(UUID.randomUUID().toString(), tipo, "ms-pedidos", LocalDateTime.now(),
				pedido.pedidoId(), null, pedido.clienteId(), pedido.total(), pedido.estado(), null);
	}
}
