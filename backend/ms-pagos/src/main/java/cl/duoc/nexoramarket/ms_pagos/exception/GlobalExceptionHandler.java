package cl.duoc.nexoramarket.ms_pagos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(PedidoNoDisponibleException.class)
	public ProblemDetail pedidoNoDisponible(PedidoNoDisponibleException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ProblemDetail recursoNoEncontrado(RecursoNoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}
}
