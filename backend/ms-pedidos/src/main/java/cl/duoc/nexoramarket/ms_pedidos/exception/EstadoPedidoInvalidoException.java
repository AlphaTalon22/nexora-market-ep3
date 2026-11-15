package cl.duoc.nexoramarket.ms_pedidos.exception;

/** La operación no se permite en el estado actual del pedido (HTTP 409). */
public class EstadoPedidoInvalidoException extends RuntimeException {

    public EstadoPedidoInvalidoException(String message) {
        super(message);
    }
}
