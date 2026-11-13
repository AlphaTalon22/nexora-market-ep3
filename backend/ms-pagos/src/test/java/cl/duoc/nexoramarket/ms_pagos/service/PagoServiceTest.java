package cl.duoc.nexoramarket.ms_pagos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.nexoramarket.ms_pagos.dto.CrearPagoRequest;
import cl.duoc.nexoramarket.ms_pagos.exception.PedidoNoDisponibleException;
import cl.duoc.nexoramarket.ms_pagos.messaging.publisher.PagoEventPublisher;
import cl.duoc.nexoramarket.ms_pagos.model.Pago;
import cl.duoc.nexoramarket.ms_pagos.model.PedidoPorPagar;
import cl.duoc.nexoramarket.ms_pagos.repository.PagoRepository;
import cl.duoc.nexoramarket.ms_pagos.repository.PedidoPorPagarRepository;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

	private static final String CLIENTE = "oid-cliente";

	@Mock
	private PagoRepository pagoRepository;

	@Mock
	private PedidoPorPagarRepository pedidoPorPagarRepository;

	@Mock
	private PagoEventPublisher pagoEventPublisher;

	@InjectMocks
	private PagoService pagoService;

	private CrearPagoRequest request(String ultimos4) {
		return new CrearPagoRequest(10L, "TARJETA_CREDITO", ultimos4);
	}

	private PedidoPorPagar pedido(String estado) {
		return new PedidoPorPagar(1L, 10L, CLIENTE, 450000.0, estado);
	}

	@Test
	void siElPedidoNoEstaDisponibleResponde409() {
		when(pedidoPorPagarRepository.findByPedidoId(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> pagoService.procesar(request("1234"), CLIENTE))
				.isInstanceOf(PedidoNoDisponibleException.class)
				.hasMessageContaining("aún no está disponible");

		verifyNoInteractions(pagoRepository, pagoEventPublisher);
	}

	@Test
	void siElPedidoYaFuePagadoResponde409() {
		when(pedidoPorPagarRepository.findByPedidoId(10L)).thenReturn(Optional.of(pedido("PAGADO")));

		assertThatThrownBy(() -> pagoService.procesar(request("1234"), CLIENTE))
				.isInstanceOf(PedidoNoDisponibleException.class)
				.hasMessageContaining("ya fue pagado o cancelado");

		verifyNoInteractions(pagoRepository, pagoEventPublisher);
	}

	@Test
	void conUltimos4CeroCeroCeroCeroElPagoEsRechazadoYElPedidoSiguePendiente() {
		PedidoPorPagar pedido = pedido("PENDIENTE");
		when(pedidoPorPagarRepository.findByPedidoId(10L)).thenReturn(Optional.of(pedido));
		when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

		Pago pago = pagoService.procesar(request("0000"), CLIENTE);

		assertThat(pago.getEstado()).isEqualTo("RECHAZADO");
		assertThat(pedido.getEstado()).isEqualTo("PENDIENTE");
		verify(pedidoPorPagarRepository, never()).save(any(PedidoPorPagar.class));
		verify(pagoEventPublisher).publicarResultadoPago(pago);
		verify(pagoEventPublisher, never()).solicitarBoleta(any(Pago.class));
	}

	@Test
	void conUltimos4ValidosElPagoEsAprobadoYElPedidoQuedaPagado() {
		PedidoPorPagar pedido = pedido("PENDIENTE");
		when(pedidoPorPagarRepository.findByPedidoId(10L)).thenReturn(Optional.of(pedido));
		when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

		Pago pago = pagoService.procesar(request("1234"), CLIENTE);

		assertThat(pago.getEstado()).isEqualTo("APROBADO");
		assertThat(pago.getClienteId()).isEqualTo(CLIENTE);
		assertThat(pago.getMonto()).isEqualTo(450000.0);
		assertThat(pedido.getEstado()).isEqualTo("PAGADO");
		verify(pedidoPorPagarRepository).save(pedido);
		verify(pagoEventPublisher).publicarResultadoPago(pago);
		verify(pagoEventPublisher).solicitarBoleta(pago);
	}
}
