package cl.duoc.nexoramarket.ms_pedidos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoRequest(
        @NotNull(message = "productoId es obligatorio") Long productoId,
        @NotNull(message = "cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 99, message = "La cantidad máxima es 99") Integer cantidad) {
}
