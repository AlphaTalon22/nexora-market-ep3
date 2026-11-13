package cl.duoc.nexoramarket.ms_pagos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional(readOnly = true)
	public List<Pago> listarPorCliente(String clienteId) {
		return pagoRepository.findByClienteId(clienteId);
	}

	@Transactional(readOnly = true)
	public Pago buscarPorPedido(String clienteId, Long pedidoId) {
		return pagoRepository.findByPedidoIdAndClienteId(pedidoId, clienteId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No hay pagos para el pedido " + pedidoId));
	}
}
