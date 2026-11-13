package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * La API de administración no debe aceptar nombres inválidos ni cantidades incoherentes.
 */
class ValidacionDtoTest {

	private static ValidatorFactory factory;
	private static Validator validator;

	@BeforeAll
	static void init() {
		factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@AfterAll
	static void close() {
		factory.close();
	}

	private Set<String> camposInvalidos(Object dto) {
		Set<ConstraintViolation<Object>> violaciones = validator.validate(dto);
		return violaciones.stream().map(v -> v.getPropertyPath().toString())
				.collect(java.util.stream.Collectors.toSet());
	}

	@Test
	void topicoValidoNoTieneErrores() {
		assertThat(camposInvalidos(new CrearTopicoRequest("nexora.demo.eventos", 3, 3,
				Map.of("retention.ms", "604800000")))).isEmpty();
	}

	@Test
	void rechazaNombreConEspacios() {
		assertThat(camposInvalidos(new CrearTopicoRequest("nexora demo", 1, 1, null)))
				.contains("nombre");
	}

	@Test
	void rechazaNombreEnBlanco() {
		assertThat(camposInvalidos(new CrearTopicoRequest("   ", 1, 1, null))).contains("nombre");
	}

	@Test
	void rechazaCeroParticiones() {
		assertThat(camposInvalidos(new CrearTopicoRequest("nexora.demo", 0, 1, null)))
				.contains("particiones");
	}

	@Test
	void rechazaCeroReplicas() {
		assertThat(camposInvalidos(new CrearTopicoRequest("nexora.demo", 1, 0, null)))
				.contains("replicacion");
	}

	@Test
	void configuracionVaciaEsInvalida() {
		assertThat(camposInvalidos(new ActualizarConfiguracionRequest(Map.of())))
				.contains("configuracion");
	}

	@Test
	void configuracionConValoresEsValida() {
		assertThat(camposInvalidos(new ActualizarConfiguracionRequest(
				Map.of("retention.ms", "86400000")))).isEmpty();
	}

	@Test
	void aumentoDeParticionesDebeSerPositivo() {
		assertThat(camposInvalidos(new AumentarParticionesRequest(0))).contains("particiones");
	}
}
