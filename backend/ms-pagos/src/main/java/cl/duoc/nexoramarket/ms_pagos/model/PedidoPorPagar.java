package cl.duoc.nexoramarket.ms_pagos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local del pedido que se desea pagar.
 * En la Fase 3 se llena desde Kafka (consumer pagos-pedidos); por ahora queda vacía.
 */
@Entity
@Table(name = "pedidos_por_pagar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPorPagar {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "El ID del pedido es obligatorio")
	@Column(nullable = false, unique = true)
	private Long pedidoId;

	@NotBlank(message = "El cliente es obligatorio")
	@Column(nullable = false)
	private String clienteId;

	@NotNull(message = "El total es obligatorio")
	@Column(nullable = false)
	private Double total;

	@NotBlank(message = "El estado es obligatorio")
	@Column(nullable = false)
	private String estado; // PENDIENTE, PAGADO, CANCELADO
}
