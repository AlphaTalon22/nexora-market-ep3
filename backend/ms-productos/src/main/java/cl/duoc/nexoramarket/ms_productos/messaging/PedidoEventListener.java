package cl.duoc.nexoramarket.ms_productos.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import cl.duoc.nexoramarket.ms_productos.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Consumer group {@code productos-stock}: lee los eventos de pedidos y delega en
 * {@link StockService}. Tres hilos (uno por partición del tópico).
 */
@Component
@RequiredArgsConstructor
public class PedidoEventListener {

    private final StockService stockService;

    @KafkaListener(topics = Topicos.PEDIDOS, groupId = "productos-stock",
            containerFactory = "pedidoListenerContainerFactory", concurrency = "3")
    public void escuchar(@Valid PedidoEvento evento,
                         @Header(KafkaHeaders.RECEIVED_PARTITION) int particion,
                         @Header(KafkaHeaders.OFFSET) long offset) {
        stockService.procesar(evento, particion, offset);
    }
}
