package cl.duoc.nexoramarket.ms_pedidos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copia local del catálogo construida desde nexora.inventario.eventos
 * (consumer pedidos-catalogo). Permite calcular el total del pedido sin
 * llamar por HTTP a ms-productos: si ms-productos cae, igual se pueden crear pedidos.
 */
@Entity
@Table(name = "productos_catalogo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCatalogo {

    @Id
    private Long productoId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private LocalDateTime actualizadoEn;
}
