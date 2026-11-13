package cl.duoc.nexoramarket.ms_pagos.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

import cl.duoc.nexoramarket.ms_pagos.model.Pago;

/**
 * Evento de dominio que viaja como JSON por {@code nexora.eventos.topic}.
 * Mismo contrato que usan ms-pedidos y ms-notificaciones.
 */
public record EventoNegocio(
		String eventoId,
		String tipo,
		String origen,
		LocalDateTime ocurridoEn,
		Long pedidoId,
		Long pagoId,
		String clienteId,
		Double monto,
		String estado,
		String metodoPago) {

	/** Construye el evento con el resultado de un pago. */
	public static EventoNegocio desdePago(String tipo, Pago pago) {
		return new EventoNegocio(UUID.randomUUID().toString(), tipo, "ms-pagos", LocalDateTime.now(),
				pago.getPedidoId(), pago.getId(), pago.getClienteId(), pago.getMonto(), pago.getEstado(),
				pago.getMetodoPago());
	}
}
