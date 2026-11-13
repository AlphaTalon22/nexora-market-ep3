package cl.duoc.nexoramarket.common.kafka;

import java.time.LocalDateTime;

/**
 * Evento de inventario publicado por ms-productos en {@code nexora.inventario.eventos}
 * (key = productoId, tópico compactado con el estado completo del producto).
 * <p>
 * Lo consumen ms-pedidos (grupo {@code pedidos-catalogo}, para calcular totales sin
 * llamadas HTTP) y ms-analitica.
 */
public record InventarioEvento(
		String eventoId,
		TipoEventoInventario tipo,
		String origen,
		LocalDateTime ocurridoEn,
		Long productoId,
		String sku,
		String nombre,
		String categoria,
		Double precio,
		Integer stockAnterior,
		Integer stockActual,
		Integer stockMinimo,
		boolean activo,
		Long pedidoId) {
}
