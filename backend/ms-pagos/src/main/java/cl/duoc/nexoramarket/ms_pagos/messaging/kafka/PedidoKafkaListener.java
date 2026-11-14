package cl.duoc.nexoramarket.ms_pagos.messaging.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import cl.duoc.nexoramarket.ms_pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Consumer del grupo {@code pagos-pedidos}: PEDIDO_CREADO crea la copia local
 * PENDIENTE y PEDIDO_CANCELADO la cancela. Ambos casos son idempotentes.
 */
@Component
@RequiredArgsConstructor
public class PedidoKafkaListener {

	private final PagoService pagoService;

	@KafkaListener(topics = Topicos.PEDIDOS, groupId = "pagos-pedidos",
			containerFactory = "pagosListenerContainerFactory", concurrency = "3")
	public void escuchar(@Valid PedidoEvento evento,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int particion,
			@Header(KafkaHeaders.OFFSET) long offset) {
		switch (evento.tipo()) {
			case PEDIDO_CREADO -> pagoService.registrarPedido(evento);
			case PEDIDO_CANCELADO -> pagoService.cancelarPedido(evento);
			default -> {
				// PEDIDO_PAGADO y PEDIDO_RECHAZADO no cambian la copia local
			}
		}
	}
}
