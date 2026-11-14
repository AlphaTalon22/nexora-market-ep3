package cl.duoc.nexoramarket.ms_analitica.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Marca de idempotencia: un evento de Kafka procesado una sola vez.
 * La PK es el eventoId que publica el microservicio productor.
 */
@Entity
@Table(name = "eventos_procesados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcesado {

	@Id
	@Column(name = "evento_id", length = 64, nullable = false)
	private String eventoId;

	@Column(nullable = false)
	private String topico;

	@Column(nullable = false)
	private String tipo;

	@Column(nullable = false)
	private Integer particion;

	@Column(name = "offset_kafka", nullable = false)
	private Long offset;

	@Column(name = "procesado_en", nullable = false)
	private LocalDateTime procesadoEn;
}
