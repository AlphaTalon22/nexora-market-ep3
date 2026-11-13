package cl.duoc.nexoramarket.ms_notificaciones.boleta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.nexoramarket.ms_notificaciones.common.MensajeInvalidoException;
import cl.duoc.nexoramarket.ms_notificaciones.common.ValidadorMensajes;
import cl.duoc.nexoramarket.ms_notificaciones.messaging.contract.GenerarBoletaCommand;
import cl.duoc.nexoramarket.ms_notificaciones.model.Boleta;
import cl.duoc.nexoramarket.ms_notificaciones.repository.BoletaRepository;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

	private static ValidatorFactory factory;
	private static ValidadorMensajes validador;

	@Mock
	private BoletaRepository boletaRepository;

	private BoletaService service;

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
		service = new BoletaService(validador, boletaRepository);
	}

	private GenerarBoletaCommand comando(Double monto) {
		return new GenerarBoletaCommand("cmd-1", 3L, 10L, "oid-cliente", monto, "TARJETA_DEBITO",
				LocalDateTime.of(2026, 10, 2, 12, 0));
	}

	@Test
	void guardaLaBoletaNuevaEnLaBaseDeDatos() {
		when(boletaRepository.findByPedidoId(10L)).thenReturn(Optional.empty());
		when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> {
			Boleta b = inv.getArgument(0);
			b.setId(1L);
			return b;
		});

		Boleta boleta = service.generar(comando(450000.0));

		assertThat(boleta.getId()).isEqualTo(1L);
		assertThat(boleta.getPedidoId()).isEqualTo(10L);
		assertThat(boleta.getPagoId()).isEqualTo(3L);
		assertThat(boleta.getClienteId()).isEqualTo("oid-cliente");
		assertThat(boleta.getMonto()).isEqualTo(450000.0);
		verify(boletaRepository).save(any(Boleta.class));
	}

	@Test
	void mensajeDuplicadoNoGuardaOtraBoleta() {
		Boleta existente = new Boleta(1L, 3L, 10L, "oid-cliente", 450000.0, "TARJETA_DEBITO",
				LocalDateTime.of(2026, 10, 2, 12, 0));
		when(boletaRepository.findByPedidoId(10L)).thenReturn(Optional.of(existente));

		Boleta boleta = service.generar(comando(450000.0));

		assertThat(boleta).isSameAs(existente);
		verify(boletaRepository, never()).save(any(Boleta.class));
	}

	@Test
	void montoNegativoEsInvalido() {
		assertThatThrownBy(() -> service.generar(comando(-1.0)))
				.isInstanceOf(MensajeInvalidoException.class)
				.hasMessageContaining("monto");

		verifyNoInteractions(boletaRepository);
	}

	@Test
	void comandoSinClienteEsInvalido() {
		GenerarBoletaCommand sinCliente = new GenerarBoletaCommand("cmd-1", 3L, 10L, null, 450000.0,
				"TARJETA_DEBITO", LocalDateTime.now());

		assertThatThrownBy(() -> service.generar(sinCliente))
				.isInstanceOf(MensajeInvalidoException.class)
				.hasMessageContaining("clienteId");

		verifyNoInteractions(boletaRepository);
	}
}
