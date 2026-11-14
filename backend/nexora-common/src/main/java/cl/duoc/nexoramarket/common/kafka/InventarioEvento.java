package cl.duoc.nexoramarket.common.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Evento de inventario publicado por ms-productos en {@code nexora.inventario.eventos}
 * (key = productoId, tópico compactado con el estado completo del producto).
 * <p>
 * Lo consumen ms-pedidos (grupo {@code pedidos-catalogo}, para calcular totales sin
 * llamadas HTTP) y ms-analitica.
 */
public record InventarioEvento(
		@NotBlank String eventoId,
		@NotNull TipoEventoInventario tipo,
		String origen,
		LocalDateTime ocurridoEn,
		@NotNull Long productoId,
		String sku,
		String nombre,
		String categoria,
		@NotNull Double precio,
		Integer stockAnterior,
		@NotNull Integer stockActual,
		Integer stockMinimo,
		boolean activo,
		Long pedidoId) {
}
