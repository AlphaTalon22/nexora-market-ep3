package cl.duoc.nexoramarket.ms_analitica.model;

import java.time.LocalDateTime;

import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una fila por cada evento de pedido recibido (creado, pagado, rechazado o cancelado).
 * Las métricas distinguen por {@link #tipo}: pedidos = PEDIDO_CREADO y ventas/ingreso =
 * solo PEDIDO_PAGADO.
 */
@Entity
@Table(name = "eventos_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoPedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "evento_id", nullable = false, unique = true, length = 64)
	private String eventoId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TipoEventoPedido tipo;

	@Column(name = "pedido_id", nullable = false)
	private Long pedidoId;

	@Column(name = "cliente_id", nullable = false)
	private String clienteId;

	@Column(nullable = false)
	private Double total;

	@Column(nullable = false)
	private LocalDateTime fecha;
}
