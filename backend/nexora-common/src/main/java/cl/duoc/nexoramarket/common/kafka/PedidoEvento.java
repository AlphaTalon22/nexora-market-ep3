package cl.duoc.nexoramarket.common.kafka;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
		@NotBlank String eventoId,
		@NotNull TipoEventoPedido tipo,
		String origen,
		LocalDateTime ocurridoEn,
		@NotNull Long pedidoId,
		@NotBlank String clienteId,
		@NotEmpty @Valid List<ItemPedidoEvento> items,
		@NotNull Double total,
		String estado,
		String metodoPago) {
}
