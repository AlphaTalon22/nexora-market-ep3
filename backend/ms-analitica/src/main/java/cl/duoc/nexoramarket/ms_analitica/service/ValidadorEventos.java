package cl.duoc.nexoramarket.ms_analitica.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import cl.duoc.nexoramarket.ms_analitica.exception.EventoInvalidoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Valida el contenido de un evento con las anotaciones de Bean Validation del
 * contrato compartido. Si algo no cumple, lanza {@link EventoInvalidoException}
 * (error no recuperable → DLT).
 */
@Component
public class ValidadorEventos {

	private final Validator validator;

	public ValidadorEventos(Validator validator) {
		this.validator = validator;
	}

	public <T> void validar(T evento) {
		if (evento == null) {
			throw new EventoInvalidoException("El evento llegó vacío");
		}
		Set<ConstraintViolation<T>> violaciones = validator.validate(evento);
		if (!violaciones.isEmpty()) {
			String detalle = violaciones.stream()
					.map(v -> v.getPropertyPath() + " " + v.getMessage())
					.sorted()
					.collect(Collectors.joining(", "));
			throw new EventoInvalidoException(
					evento.getClass().getSimpleName() + " inválido: " + detalle);
		}
	}
}
