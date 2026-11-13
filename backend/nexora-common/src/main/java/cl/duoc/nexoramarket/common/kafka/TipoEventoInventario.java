package cl.duoc.nexoramarket.common.kafka;

/**
 * Tipos de evento que viajan por {@code nexora.inventario.eventos}.
 */
public enum TipoEventoInventario {

	PRODUCTO_CREADO,

	PRODUCTO_ACTUALIZADO,

	STOCK_DESCONTADO
}
