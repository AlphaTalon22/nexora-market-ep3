package cl.duoc.nexoramarket.ms_pagos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.ms_pagos.dto.CrearPagoRequest;
import cl.duoc.nexoramarket.ms_pagos.exception.PedidoNoDisponibleException;
import cl.duoc.nexoramarket.ms_pagos.exception.RecursoNoEncontradoException;
import cl.duoc.nexoramarket.ms_pagos.messaging.publisher.PagoEventPublisher;
import cl.duoc.nexoramarket.ms_pagos.model.Pago;
import cl.duoc.nexoramarket.ms_pagos.model.PedidoPorPagar;
import cl.duoc.nexoramarket.ms_pagos.repository.PagoRepository;
import cl.duoc.nexoramarket.ms_pagos.repository.PedidoPorPagarRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagoService {

	private static final String ESTADO_PENDIENTE = "PENDIENTE";
	private static final String ESTADO_PAGADO = "PAGADO";
	private static final String ESTADO_CANCELADO = "CANCELADO";
	private static final String ESTADO_APROBADO = "APROBADO";
	private static final String ESTADO_RECHAZADO = "RECHAZADO";
	private static final String TARJETA_RECHAZADA = "0000";

	private final PagoRepository pagoRepository;
	private final PedidoPorPagarRepository pedidoPorPagarRepository;
	private final PagoEventPublisher pagoEventPublisher;

	@Transactional
	public Pago procesar(CrearPagoRequest request, String clienteId) {
		PedidoPorPagar pedido = pedidoPorPagarRepository.findByPedidoId(request.pedidoId())
				.orElseThrow(() -> new PedidoNoDisponibleException("El pedido aún no está disponible"));

		if (!ESTADO_PENDIENTE.equals(pedido.getEstado())) {
			throw new PedidoNoDisponibleException("El pedido ya fue pagado o cancelado");
		}

		Pago pago = new Pago();
		pago.setPedidoId(pedido.getPedidoId());
		pago.setClienteId(clienteId);
		pago.setMonto(pedido.getTotal());
		pago.setMetodoPago(request.metodoPago());
		pago.setUltimos4(request.ultimos4());
		pago.setEstado(TARJETA_RECHAZADA.equals(request.ultimos4()) ? ESTADO_RECHAZADO : ESTADO_APROBADO);
		pago.setFechaPago(LocalDateTime.now());

		Pago guardado = pagoRepository.save(pago);

		if (ESTADO_APROBADO.equals(guardado.getEstado())) {
			pedido.setEstado(ESTADO_PAGADO);
			pedidoPorPagarRepository.save(pedido);
			pagoEventPublisher.publicarResultadoPago(guardado);
			pagoEventPublisher.solicitarBoleta(guardado);
		}
		else {
			pagoEventPublisher.publicarResultadoPago(guardado);
		}

		return guardado;
	}

	/**
	 * Crea la copia local del pedido por pagar. Es idempotente: si el pedido ya
	 * existe (por un evento duplicado) no se vuelve a guardar.
	 */
	@Transactional
	public void registrarPedido(PedidoEvento evento) {
		if (pedidoPorPagarRepository.findByPedidoId(evento.pedidoId()).isPresent()) {
			return;
		}
		PedidoPorPagar pedido = new PedidoPorPagar();
		pedido.setPedidoId(evento.pedidoId());
		pedido.setClienteId(evento.clienteId());
		pedido.setTotal(evento.total());
		pedido.setEstado(ESTADO_PENDIENTE);
		pedidoPorPagarRepository.save(pedido);
	}

	/**
	 * Cancela la copia local solo si sigue PENDIENTE. Es idempotente: un pedido
	 * ya cancelado o pagado no se toca.
	 */
	@Transactional
	public void cancelarPedido(PedidoEvento evento) {
		pedidoPorPagarRepository.findByPedidoId(evento.pedidoId())
				.filter(pedido -> ESTADO_PENDIENTE.equals(pedido.getEstado()))
				.ifPresent(pedido -> {
					pedido.setEstado(ESTADO_CANCELADO);
					pedidoPorPagarRepository.save(pedido);
				});
	}

	@Transactional(readOnly = true)
	public List<Pago> listarPorCliente(String clienteId) {
		return pagoRepository.findByClienteId(clienteId);
	}

	@Transactional(readOnly = true)
	public Pago buscarPorPedido(String clienteId, Long pedidoId) {
		return pagoRepository.findFirstByPedidoIdAndClienteIdOrderByFechaPagoDesc(pedidoId, clienteId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No hay pagos para el pedido " + pedidoId));
	}
}
