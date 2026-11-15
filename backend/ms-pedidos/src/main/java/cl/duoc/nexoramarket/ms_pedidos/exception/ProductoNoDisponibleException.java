package cl.duoc.nexoramarket.ms_pedidos.exception;

/** El producto no existe en el catálogo local, está inactivo o no tiene stock suficiente (HTTP 409). */
public class ProductoNoDisponibleException extends RuntimeException {

    public ProductoNoDisponibleException(String message) {
        super(message);
    }
}
