package cl.duoc.nexoramarket.ms_pedidos.dto;

import java.time.LocalDateTime;
import java.util.List;

import cl.duoc.nexoramarket.ms_pedidos.model.Pedido;

/** Forma que espera el frontend (models/pedido.ts e item-carrito.ts). */
public record PedidoResponse(
        Long id,
        String clienteId,
        LocalDateTime fecha,
        Double total,
        String estado,
        List<Item> items) {

    public record Item(Long productoId, String nombreProducto, Double precioUnitario, Integer cantidad) {
    }

    public static PedidoResponse desde(Pedido pedido) {
        List<Item> items = pedido.getItems().stream()
                .map(i -> new Item(i.getProductoId(), i.getNombreProducto(), i.getPrecioUnitario(), i.getCantidad()))
                .toList();
        return new PedidoResponse(pedido.getId(), pedido.getClienteId(), pedido.getFecha(),
                pedido.getTotal(), pedido.getEstado(), items);
    }
}
