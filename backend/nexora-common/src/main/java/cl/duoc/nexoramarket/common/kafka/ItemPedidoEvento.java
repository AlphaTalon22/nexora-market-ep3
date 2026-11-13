package cl.duoc.nexoramarket.common.kafka;

/**
 * Ítem del pedido dentro de un {@link PedidoEvento}.
 */
public record ItemPedidoEvento(
		Long productoId,
		Integer cantidad,
		Double precioUnitario,
		Double subtotal) {
}
