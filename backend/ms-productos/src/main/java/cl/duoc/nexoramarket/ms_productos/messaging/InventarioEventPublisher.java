package cl.duoc.nexoramarket.ms_productos.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Productor de {@code nexora.inventario.eventos}.
 * <p>
 * Escucha el evento interno que publica el service y lo envía a Kafka recién
 * cuando la transacción hizo commit: si la BD falla no sale ningún evento
 * "fantasma". La key es el productoId, así todos los cambios de un mismo
 * producto caen en la misma partición y se consumen en orden.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventarioEventPublisher {

    private final KafkaTemplate<String, InventarioEvento> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicar(InventarioEvento evento) {
        String key = String.valueOf(evento.productoId());
        try {
            kafkaTemplate.send(Topicos.INVENTARIO, key, evento).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("No se pudo publicar {} del producto {} (eventoId={}): {}",
                            evento.tipo(), key, evento.eventoId(), error.getMessage());
                } else if (resultado != null) {
                    log.info("Publicado {} del producto {} en {}-{} offset {}", evento.tipo(), key,
                            resultado.getRecordMetadata().topic(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset());
                }
            });
        } catch (RuntimeException e) {
            // Kafka caído: el cambio ya quedó en la BD; se registra y no se rompe la petición
            log.error("Kafka no disponible al publicar {} del producto {}: {}",
                    evento.tipo(), key, e.getMessage());
        }
    }
}
