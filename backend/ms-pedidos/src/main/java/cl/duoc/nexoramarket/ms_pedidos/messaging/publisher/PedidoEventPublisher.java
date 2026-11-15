package cl.duoc.nexoramarket.ms_pedidos.messaging.publisher;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;

/**
 * Publicación del aviso "pedido creado" por RabbitMQ (notificaciones al cliente).
 * Cómo y dónde se publica queda encapsulado en la implementación.
 */
public interface PedidoEventPublisher {

	void publicarPedidoCreado(PedidoEvento pedido);
}
