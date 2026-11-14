package cl.duoc.nexoramarket.ms_analitica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una fila por cada ítem del evento de pedido. El top de productos solo cuenta
 * ítems de eventos PEDIDO_PAGADO.
 */
@Entity
@Table(name = "items_evento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemEvento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "evento_id", nullable = false)
	private EventoPedido evento;

	@Column(name = "producto_id", nullable = false)
	private Long productoId;

	@Column(nullable = false)
	private Integer cantidad;

	@Column(name = "precio_unitario", nullable = false)
	private Double precioUnitario;
}
