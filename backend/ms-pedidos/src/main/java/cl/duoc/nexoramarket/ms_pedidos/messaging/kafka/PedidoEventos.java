package cl.duoc.nexoramarket.ms_pedidos.messaging.kafka;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import cl.duoc.nexoramarket.common.kafka.ItemPedidoEvento;
import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_pedidos.model.Pedido;

/**
 * Construye el {@link PedidoEvento} con todo lo que necesitan los consumidores:
 * ms-pagos (pedidoId, clienteId, total), ms-productos (ítems y cantidades para
 * descontar stock) y ms-analitica (ítems, precios y total para las métricas).
 */
public final class PedidoEventos {

    public static final String ORIGEN = "ms-pedidos";

    private PedidoEventos() {
    }

    public static PedidoEvento de(TipoEventoPedido tipo, Pedido pedido, String metodoPago) {
        List<ItemPedidoEvento> items = pedido.getItems().stream()
                .map(i -> new ItemPedidoEvento(i.getProductoId(), i.getCantidad(), i.getPrecioUnitario(),
                        i.subtotal()))
                .toList();
        return new PedidoEvento(UUID.randomUUID().toString(), tipo, ORIGEN, LocalDateTime.now(),
                pedido.getId(), pedido.getClienteId(), items, pedido.getTotal(), pedido.getEstado(), metodoPago);
    }
}
