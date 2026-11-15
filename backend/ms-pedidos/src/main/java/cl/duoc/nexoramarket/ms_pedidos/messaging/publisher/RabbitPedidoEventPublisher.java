package cl.duoc.nexoramarket.ms_pedidos.messaging.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_pedidos.messaging.config.MessagingProperties;
import cl.duoc.nexoramarket.ms_pedidos.messaging.event.EventoNegocio;

/**
 * Productor RabbitMQ: publica {@code pedido.creado} en el exchange TOPIC de eventos.
 * <p>
 * Kafka complementa a RabbitMQ sin reemplazarlo: el mismo evento interno que va a
 * Kafka dispara también este aviso, igual que en la evaluación anterior. Si RabbitMQ
 * no está disponible, el pedido igual queda guardado y el fallo queda en el log.
 */
@Component
public class RabbitPedidoEventPublisher implements PedidoEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(RabbitPedidoEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;
	private final MessagingProperties props;

	public RabbitPedidoEventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties props) {
		this.rabbitTemplate = rabbitTemplate;
		this.props = props;
	}

	/** Después del commit, solo para PEDIDO_CREADO. */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void alConfirmarse(PedidoEvento evento) {
		if (evento.tipo() == TipoEventoPedido.PEDIDO_CREADO) {
			publicarPedidoCreado(evento);
		}
	}

	@Override
	public void publicarPedidoCreado(PedidoEvento pedido) {
		EventoNegocio evento = EventoNegocio.pedidoCreado(props.routingKeys().pedidoCreado(), pedido);
		try {
			rabbitTemplate.convertAndSend(props.exchanges().eventos(), evento.tipo(), evento,
					new CorrelationData(evento.eventoId()));
			log.info("[PUBLICADO] {} -> {} (pedidoId={}, eventoId={})", evento.tipo(), props.exchanges().eventos(),
					pedido.pedidoId(), evento.eventoId());
		}
		catch (AmqpException ex) {
			log.error("[PUBLICACION FALLIDA] No se pudo publicar {} del pedido {}: {}. El pedido sí quedó guardado.",
					evento.tipo(), pedido.pedidoId(), ex.getMessage());
		}
	}
}
