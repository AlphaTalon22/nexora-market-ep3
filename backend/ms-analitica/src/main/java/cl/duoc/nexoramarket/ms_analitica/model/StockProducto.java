package cl.duoc.nexoramarket.ms_analitica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estado actual de cada producto según el último {@code InventarioEvento}.
 * Las alertas salen de comparar stockActual contra stockMinimo.
 */
@Entity
@Table(name = "stock_productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockProducto {

	@Id
	@Column(name = "producto_id", nullable = false)
	private Long productoId;

	@Column(nullable = false)
	private String nombre;

	@Column(name = "stock_actual", nullable = false)
	private Integer stockActual;

	@Column(name = "stock_minimo", nullable = false)
	private Integer stockMinimo;
}
