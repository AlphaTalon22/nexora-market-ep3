package cl.duoc.nexoramarket.common.kafka;

import jakarta.validation.constraints.NotNull;

/**
 * Ítem del pedido dentro de un {@link PedidoEvento}.
 */
public record ItemPedidoEvento(
		@NotNull Long productoId,
		@NotNull Integer cantidad,
		@NotNull Double precioUnitario,
		Double subtotal) {
}
