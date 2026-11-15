package cl.duoc.nexoramarket.ms_productos.service;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.common.kafka.ItemPedidoEvento;
import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoInventario;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import cl.duoc.nexoramarket.ms_productos.messaging.InventarioEventos;
import cl.duoc.nexoramarket.ms_productos.model.EventoProcesado;
import cl.duoc.nexoramarket.ms_productos.model.Producto;
import cl.duoc.nexoramarket.ms_productos.repository.EventoProcesadoRepository;
import cl.duoc.nexoramarket.ms_productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lógica de negocio del consumer {@code productos-stock}: cuando un pedido queda
 * PAGADO descuenta el stock de cada ítem y publica STOCK_DESCONTADO.
 * Es idempotente: un eventoId ya procesado no vuelve a descontar.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductoRepository productoRepository;
    private final EventoProcesadoRepository eventoProcesadoRepository;
    private final ValidadorEventos validadorEventos;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public void procesar(PedidoEvento evento, int particion, long offset) {
        validadorEventos.validar(evento);

        if (evento.tipo() != TipoEventoPedido.PEDIDO_PAGADO) {
            log.debug("Evento {} del pedido {} no afecta el stock", evento.tipo(), evento.pedidoId());
            return;
        }
        if (eventoProcesadoRepository.existsById(evento.eventoId())) {
            log.info("Evento {} ya procesado (idempotencia), no se descuenta de nuevo", evento.eventoId());
            return;
        }
        eventoProcesadoRepository.save(new EventoProcesado(evento.eventoId(), Topicos.PEDIDOS,
                evento.tipo().name(), particion, offset, LocalDateTime.now()));

        for (ItemPedidoEvento item : evento.items()) {
            productoRepository.findByIdParaActualizar(item.productoId()).ifPresentOrElse(
                    producto -> descontar(producto, item.cantidad(), evento.pedidoId()),
                    () -> log.warn("Pedido {}: el producto {} no existe, se omite",
                            evento.pedidoId(), item.productoId()));
        }
    }

    private void descontar(Producto producto, int cantidad, Long pedidoId) {
        int stockAnterior = producto.getStock();
        if (cantidad > stockAnterior) {
            log.warn("Pedido {}: stock insuficiente del producto {} ({} < {}), queda en 0",
                    pedidoId, producto.getId(), stockAnterior, cantidad);
        }
        producto.setStock(Math.max(0, stockAnterior - cantidad));
        productoRepository.save(producto);
        log.info("Pedido {}: producto {} stock {} -> {}", pedidoId, producto.getId(),
                stockAnterior, producto.getStock());

        eventos.publishEvent(InventarioEventos.de(TipoEventoInventario.STOCK_DESCONTADO,
                producto, stockAnterior, pedidoId));
    }
}
