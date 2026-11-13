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
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String clienteId; // oid de Entra ID

	@Column(nullable = false)
	private String tipo; // PEDIDO, PAGO, BOLETA

	@Column(nullable = false, length = 500)
	private String mensaje;

	@Column(nullable = false)
	private LocalDateTime fecha;

	@Column(nullable = false)
	private boolean leida;
}
