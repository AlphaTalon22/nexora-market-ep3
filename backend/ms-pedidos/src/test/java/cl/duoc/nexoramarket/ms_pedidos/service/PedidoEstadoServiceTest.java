package cl.duoc.nexoramarket.ms_pedidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_pedidos.exception.MensajeInvalidoException;
import cl.duoc.nexoramarket.ms_pedidos.exception.PedidoNoEncontradoException;
import cl.duoc.nexoramarket.ms_pedidos.messaging.event.EventoNegocio;
import cl.duoc.nexoramarket.ms_pedidos.model.ItemPedido;
import cl.duoc.nexoramarket.ms_pedidos.model.Pedido;
import cl.duoc.nexoramarket.ms_pedidos.repository.PedidoRepository;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

class PedidoEstadoServiceTest {

	private static ValidatorFactory factory;

	private PedidoRepository repository;
	private ApplicationEventPublisher eventos;
	private PedidoEstadoService service;

	@BeforeAll
	static void initValidator() {
		factory = Validation.buildDefaultValidatorFactory();
	}

	@AfterAll
	static void closeValidator() {
		factory.close();
	}

	@BeforeEach
	void setUp() {
		repository = mock(PedidoRepository.class);
		eventos = mock(ApplicationEventPublisher.class);
		service = new PedidoEstadoService(repository, factory.getValidator(), eventos);
		when(repository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	private EventoNegocio pago(String tipo, Long pedidoId) {
		return new EventoNegocio("ev-1", tipo, "ms-pagos", LocalDateTime.now(), pedidoId, 3L, "cliente-1",
				450000.0, "APROBADO", "DEBITO");
	}

	private Pedido pedido(Long id, String estado) {
		Pedido pedido = new Pedido(id, "cliente-1", LocalDateTime.now(), 450000.0, estado);
		pedido.agregarItem(new ItemPedido(5L, "Monitor", 225000.0, 2));
		return pedido;
	}

	private PedidoEvento eventoPublicado() {
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(eventos).publishEvent(captor.capture());
		return (PedidoEvento) captor.getValue();
	}

	@Test
	void pagoAprobadoDejaElPedidoPagadoYPublicaPedidoPagado() {
		when(repository.findById(10L)).thenReturn(Optional.of(pedido(10L, Pedido.PENDIENTE)));

		Pedido actualizado = service.aplicarResultadoPago(pago("pago.aprobado", 10L));

		assertThat(actualizado.getEstado()).isEqualTo(Pedido.PAGADO);
		PedidoEvento evento = eventoPublicado();
		assertThat(evento.tipo()).isEqualTo(TipoEventoPedido.PEDIDO_PAGADO);
		assertThat(evento.pedidoId()).isEqualTo(10L);
		assertThat(evento.metodoPago()).isEqualTo("DEBITO");
		assertThat(evento.items()).hasSize(1);
	}

	@Test
	void pagoRechazadoDejaElPedidoRechazadoYPublicaPedidoRechazado() {
		when(repository.findById(10L)).thenReturn(Optional.of(pedido(10L, Pedido.PENDIENTE)));

		assertThat(service.aplicarResultadoPago(pago("pago.rechazado", 10L)).getEstado())
				.isEqualTo(Pedido.RECHAZADO);
		assertThat(eventoPublicado().tipo()).isEqualTo(TipoEventoPedido.PEDIDO_RECHAZADO);
	}

	@Test
	void pedidoInexistenteEsNoRecuperable() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.aplicarResultadoPago(pago("pago.aprobado", 99L)))
				.isInstanceOf(PedidoNoEncontradoException.class);
	}

	@Test
	void eventoSinPedidoEsInvalido() {
		assertThatThrownBy(() -> service.aplicarResultadoPago(pago("pago.aprobado", null)))
				.isInstanceOf(MensajeInvalidoException.class)
				.hasMessageContaining("pedidoId");
	}

	@Test
	void eventoRepetidoNoVuelveAGuardarNiPublicar() {
		when(repository.findById(10L)).thenReturn(Optional.of(pedido(10L, Pedido.PAGADO)));

		service.aplicarResultadoPago(pago("pago.aprobado", 10L));

		verify(repository, never()).save(any(Pedido.class));
		verifyNoInteractions(eventos);
	}
}
