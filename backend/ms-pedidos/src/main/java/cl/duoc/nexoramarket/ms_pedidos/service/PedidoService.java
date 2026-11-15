package cl.duoc.nexoramarket.ms_pedidos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_pedidos.dto.CrearPedidoRequest;
import cl.duoc.nexoramarket.ms_pedidos.dto.ItemPedidoRequest;
import cl.duoc.nexoramarket.ms_pedidos.exception.EstadoPedidoInvalidoException;
import cl.duoc.nexoramarket.ms_pedidos.exception.PedidoNoEncontradoException;
import cl.duoc.nexoramarket.ms_pedidos.exception.ProductoNoDisponibleException;
import cl.duoc.nexoramarket.ms_pedidos.messaging.kafka.PedidoEventos;
import cl.duoc.nexoramarket.ms_pedidos.model.ItemPedido;
import cl.duoc.nexoramarket.ms_pedidos.model.Pedido;
import cl.duoc.nexoramarket.ms_pedidos.model.ProductoCatalogo;
import cl.duoc.nexoramarket.ms_pedidos.repository.PedidoRepository;
import cl.duoc.nexoramarket.ms_pedidos.repository.ProductoCatalogoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Casos de uso REST del pedido. Cada cambio publica un PedidoEvento interno que
 * PedidoKafkaPublisher (Kafka) y RabbitPedidoEventPublisher (RabbitMQ) envían
 * después del commit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoCatalogoRepository catalogoRepository;
    private final ApplicationEventPublisher eventos;

    @Transactional(readOnly = true)
    public List<Pedido> listar(String clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public Pedido obtener(Long id, String clienteId) {
        return pedidoRepository.findByIdAndClienteId(id, clienteId)
                .orElseThrow(() -> new PedidoNoEncontradoException(id));
    }

    /**
     * Crea el pedido PENDIENTE con los precios del catálogo local (nunca los del
     * cliente) y publica PEDIDO_CREADO.
     */
    @Transactional
    public Pedido crear(String clienteId, CrearPedidoRequest request) {
        Pedido pedido = new Pedido(null, clienteId, LocalDateTime.now(), 0.0, Pedido.PENDIENTE);

        for (ItemPedidoRequest linea : request.items()) {
            ProductoCatalogo producto = catalogoRepository.findById(linea.productoId())
                    .filter(ProductoCatalogo::isActivo)
                    .orElseThrow(() -> new ProductoNoDisponibleException(
                            "El producto " + linea.productoId() + " no está disponible"));
            if (producto.getStock() < linea.cantidad()) {
                throw new ProductoNoDisponibleException("Stock insuficiente de " + producto.getNombre()
                        + ": quedan " + producto.getStock() + " unidades");
            }
            pedido.agregarItem(new ItemPedido(producto.getProductoId(), producto.getNombre(),
                    producto.getPrecio(), linea.cantidad()));
        }
        pedido.setTotal(pedido.getItems().stream().mapToDouble(ItemPedido::subtotal).sum());

        Pedido guardado = pedidoRepository.save(pedido);
        log.info("[PEDIDO] Pedido {} creado para {} por {}", guardado.getId(), clienteId, guardado.getTotal());
        eventos.publishEvent(PedidoEventos.de(TipoEventoPedido.PEDIDO_CREADO, guardado, null));
        return guardado;
    }

    /** Solo un pedido PENDIENTE se puede cancelar; publica PEDIDO_CANCELADO. */
    @Transactional
    public Pedido cancelar(Long id, String clienteId) {
        Pedido pedido = obtener(id, clienteId);
        if (!Pedido.PENDIENTE.equals(pedido.getEstado())) {
            throw new EstadoPedidoInvalidoException(
                    "Solo se puede cancelar un pedido PENDIENTE (estado actual: " + pedido.getEstado() + ")");
        }
        pedido.setEstado(Pedido.CANCELADO);
        Pedido guardado = pedidoRepository.save(pedido);
        eventos.publishEvent(PedidoEventos.de(TipoEventoPedido.PEDIDO_CANCELADO, guardado, null));
        return guardado;
    }
}
