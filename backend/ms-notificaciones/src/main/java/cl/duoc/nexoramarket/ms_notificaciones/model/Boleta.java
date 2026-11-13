package cl.duoc.nexoramarket.ms_notificaciones.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boletas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Boleta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long pagoId;

	@Column(nullable = false, unique = true)
	private Long pedidoId;

	@Column(nullable = false)
	private String clienteId; // oid de Entra ID

	@Column(nullable = false)
	private Double monto;

	@Column(nullable = false)
	private String metodoPago;

	@Column(nullable = false)
	private LocalDateTime fecha;
}
