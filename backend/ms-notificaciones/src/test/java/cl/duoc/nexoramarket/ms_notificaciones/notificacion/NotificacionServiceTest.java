package cl.duoc.nexoramarket.ms_notificaciones.notificacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.nexoramarket.ms_notificaciones.common.ErrorTransitorioException;
import cl.duoc.nexoramarket.ms_notificaciones.common.MensajeInvalidoException;
import cl.duoc.nexoramarket.ms_notificaciones.common.ValidadorMensajes;
import cl.duoc.nexoramarket.ms_notificaciones.messaging.contract.EventoNegocio;
import cl.duoc.nexoramarket.ms_notificaciones.model.Notificacion;
import cl.duoc.nexoramarket.ms_notificaciones.repository.NotificacionRepository;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

	private static ValidatorFactory factory;
	private static ValidadorMensajes validador;

	@Mock
	private NotificacionRepository notificacionRepository;

	private NotificacionService service;

	@BeforeAll
	static void initValidator() {
		factory = Validation.buildDefaultValidatorFactory();
		validador = new ValidadorMensajes(factory.getValidator());
	}

	@AfterAll
	static void closeValidator() {
		factory.close();
	}

	@BeforeEach
	void setUp() {
		service = new NotificacionService(validador, notificacionRepository, false);
	}

	private EventoNegocio evento(String tipo, Long pedidoId) {
		return new EventoNegocio("ev-1", tipo, "test", LocalDateTime.now(),
				pedidoId, 5L, "oid-cliente", 450000.0, "APROBADO", "TARJETA_DEBITO");
	}

	@Test
	void construyeMensajeSegunTipoDeEvento() {
		assertThat(service.notificar(evento("pedido.creado", 10L))).contains("Recibimos tu pedido #10");
		assertThat(service.notificar(evento("pago.aprobado", 10L))).contains("APROBADO");
		assertThat(service.notificar(evento("pago.rechazado", 10L))).contains("RECHAZADO");
	}

	@Test
	void guardaLaNotificacionConElClienteYElTipoDelEvento() {
		service.notificar(evento("pago.aprobado", 10L));

		ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
		verify(notificacionRepository).save(captor.capture());
		Notificacion guardada = captor.getValue();

		assertThat(guardada.getClienteId()).isEqualTo("oid-cliente");
		assertThat(guardada.getTipo()).isEqualTo("PAGO");
		assertThat(guardada.getMensaje()).contains("APROBADO");
		assertThat(guardada.getFecha()).isNotNull();
		assertThat(guardada.isLeida()).isFalse();
	}

	@Test
	void eventoSinPedidoEsInvalido() {
		assertThatThrownBy(() -> service.notificar(evento("pedido.creado", null)))
				.isInstanceOf(MensajeInvalidoException.class)
				.hasMessageContaining("pedidoId");
	}

	@Test
	void falloSimuladoDeCorreoEsTransitorioYNoGuarda() {
		NotificacionService conFallo = new NotificacionService(validador, notificacionRepository, true);

		assertThatThrownBy(() -> conFallo.notificar(evento("pedido.creado", 10L)))
				.isInstanceOf(ErrorTransitorioException.class);
		verify(notificacionRepository, never()).save(any(Notificacion.class));
	}
}
