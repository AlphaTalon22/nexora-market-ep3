package cl.duoc.nexoramarket.ms_pedidos.messaging.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Productor de {@code nexora.pedidos.eventos}.
 * <p>
 * Envía el evento recién cuando la transacción hizo commit (si la BD falla no
 * sale ningún evento). La key es el pedidoId: todos los eventos de un pedido
 * (CREADO → PAGADO/RECHAZADO) caen en la misma partición y se consumen en orden.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoKafkaPublisher {

    private final KafkaTemplate<String, PedidoEvento> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicar(PedidoEvento evento) {
        String key = String.valueOf(evento.pedidoId());
        try {
            kafkaTemplate.send(Topicos.PEDIDOS, key, evento).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("No se pudo publicar {} del pedido {} (eventoId={}): {}",
                            evento.tipo(), key, evento.eventoId(), error.getMessage());
                } else if (resultado != null) {
                    log.info("Publicado {} del pedido {} en {}-{} offset {}", evento.tipo(), key,
                            resultado.getRecordMetadata().topic(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset());
                }
            });
        } catch (RuntimeException e) {
            // Kafka caído: el pedido ya quedó en la BD; se registra y no se rompe la petición
            log.error("Kafka no disponible al publicar {} del pedido {}: {}", evento.tipo(), key, e.getMessage());
        }
    }
}
