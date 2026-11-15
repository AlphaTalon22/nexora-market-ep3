package cl.duoc.nexoramarket.ms_pedidos.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/** Lo que envía el frontend: solo productos y cantidades. El precio y el total los pone el backend. */
public record CrearPedidoRequest(
        @NotEmpty(message = "El pedido debe tener al menos un producto") List<@Valid ItemPedidoRequest> items) {
}
