package cl.duoc.nexoramarket.common.kafka;

/**
 * Tipos de evento que viajan por {@code nexora.pedidos.eventos}.
 */
public enum TipoEventoPedido {

	PEDIDO_CREADO,

	PEDIDO_PAGADO,

	PEDIDO_RECHAZADO,

	PEDIDO_CANCELADO
}
