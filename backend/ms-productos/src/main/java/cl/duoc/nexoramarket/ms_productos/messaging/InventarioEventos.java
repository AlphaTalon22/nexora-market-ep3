package cl.duoc.nexoramarket.ms_productos.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoInventario;
import cl.duoc.nexoramarket.ms_productos.model.Producto;

/**
 * Construye el {@link InventarioEvento} con el estado completo del producto.
 * El tópico es compactado por productoId, así que cada evento debe bastar por
 * sí solo para reconstruir el catálogo (ms-pedidos lo usa para calcular totales).
 */
public final class InventarioEventos {

    public static final String ORIGEN = "ms-productos";

    private InventarioEventos() {
    }

    public static InventarioEvento de(TipoEventoInventario tipo, Producto producto,
                                      Integer stockAnterior, Long pedidoId) {
        return de(tipo, producto, stockAnterior, pedidoId, true);
    }

    public static InventarioEvento de(TipoEventoInventario tipo, Producto producto,
                                      Integer stockAnterior, Long pedidoId, boolean activo) {
        return new InventarioEvento(
                UUID.randomUUID().toString(),
                tipo,
                ORIGEN,
                LocalDateTime.now(),
                producto.getId(),
                "SKU-" + producto.getId(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getPrecio(),
                stockAnterior,
                producto.getStock(),
                producto.getStockMinimo(),
                activo,
                pedidoId);
    }
}
