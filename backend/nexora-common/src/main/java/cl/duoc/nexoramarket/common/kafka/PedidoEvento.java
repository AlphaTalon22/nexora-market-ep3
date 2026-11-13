package cl.duoc.nexoramarket.common.kafka;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento de pedido publicado por ms-pedidos en {@code nexora.pedidos.eventos}
 * (key = pedidoId).
 * <p>
 * Lo consumen ms-pagos (grupo {@code pagos-pedidos}), ms-productos (grupo
 * {@code productos-stock} para descontar stock al pagarse) y ms-analitica.
 */
public record PedidoEvento(
		String eventoId,
		TipoEventoPedido tipo,
		String origen,
		LocalDateTime ocurridoEn,
		Long pedidoId,
		String clienteId,
		List<ItemPedidoEvento> items,
		Double total,
		String estado,
		String metodoPago) {
}
