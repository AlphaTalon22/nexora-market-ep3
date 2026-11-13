package cl.duoc.nexoramarket.ms_pagos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CrearPagoRequest(
		@NotNull(message = "El ID del pedido es obligatorio")
		Long pedidoId,

		@NotBlank(message = "El método de pago es obligatorio")
		@Pattern(regexp = "TARJETA_CREDITO|TARJETA_DEBITO|TRANSFERENCIA",
				message = "El método de pago debe ser TARJETA_CREDITO, TARJETA_DEBITO o TRANSFERENCIA")
		String metodoPago,

		@NotBlank(message = "Los últimos 4 dígitos son obligatorios")
		@Pattern(regexp = "\\d{4}", message = "Los últimos 4 dígitos deben ser exactamente 4 números")
		String ultimos4) {
}
