package cl.duoc.nexoramarket.ms_kafka_admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.nexoramarket.ms_kafka_admin.dto.ActualizarConfiguracionRequest;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.AumentarParticionesRequest;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.ClusterInfo;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.CrearTopicoRequest;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.GrupoDetalle;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.GrupoResumen;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.OperacionResponse;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.ReglasNombres;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.TopicoDetalle;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.TopicoResumen;
import cl.duoc.nexoramarket.ms_kafka_admin.service.KafkaAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

/**
 * API REST de administración de Kafka.
 *
 * <pre>
 * GET    /api/kafka-admin/cluster                                brokers + controller
 * GET    /api/kafka-admin/topicos                               tópicos con particiones y réplicas
 * GET    /api/kafka-admin/topicos/{nombre}                      detalle por partición + configuración
 * POST   /api/kafka-admin/topicos                               crear tópico           201
 * PUT    /api/kafka-admin/topicos/{nombre}/configuracion        modificar configuración
 * POST   /api/kafka-admin/topicos/{nombre}/particiones          aumentar particiones
 * DELETE /api/kafka-admin/topicos/{nombre}?forzar=true          eliminar               204
 * GET    /api/kafka-admin/grupos                                consumer groups + lag total
 * GET    /api/kafka-admin/grupos/{id}                           miembros + lag por partición
 * </pre>
 */
@RestController
@RequestMapping("/api/kafka-admin")
public class KafkaAdminController {

	private final KafkaAdminService adminService;

	public KafkaAdminController(KafkaAdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/cluster")
	public ClusterInfo cluster() {
		return adminService.cluster();
	}

	@GetMapping("/topicos")
	public List<TopicoResumen> listarTopicos() {
		return adminService.listarTopicos();
	}

	@GetMapping("/topicos/{nombre}")
	public TopicoDetalle detalleTopico(@PathVariable
			@Pattern(regexp = ReglasNombres.NOMBRE_TOPICO, message = ReglasNombres.NOMBRE_TOPICO_MENSAJE)
			String nombre) {
		return adminService.detalleTopico(nombre);
	}

	@PostMapping("/topicos")
	public ResponseEntity<OperacionResponse> crearTopico(@Valid @RequestBody CrearTopicoRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminService.crearTopico(request));
	}

	@PutMapping("/topicos/{nombre}/configuracion")
	public OperacionResponse actualizarConfiguracion(@PathVariable
			@Pattern(regexp = ReglasNombres.NOMBRE_TOPICO, message = ReglasNombres.NOMBRE_TOPICO_MENSAJE)
			String nombre,
			@Valid @RequestBody ActualizarConfiguracionRequest request) {
		return adminService.actualizarConfiguracion(nombre, request.configuracion());
	}

	@PostMapping("/topicos/{nombre}/particiones")
	public OperacionResponse aumentarParticiones(@PathVariable
			@Pattern(regexp = ReglasNombres.NOMBRE_TOPICO, message = ReglasNombres.NOMBRE_TOPICO_MENSAJE)
			String nombre,
			@Valid @RequestBody AumentarParticionesRequest request) {
		return adminService.aumentarParticiones(nombre, request.particiones());
	}

	@DeleteMapping("/topicos/{nombre}")
	public ResponseEntity<Void> eliminarTopico(@PathVariable
			@Pattern(regexp = ReglasNombres.NOMBRE_TOPICO, message = ReglasNombres.NOMBRE_TOPICO_MENSAJE)
			String nombre,
			@RequestParam(defaultValue = "false") boolean forzar) {
		adminService.eliminarTopico(nombre, forzar);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/grupos")
	public List<GrupoResumen> listarGrupos() {
		return adminService.listarGrupos();
	}

	@GetMapping("/grupos/{id}")
	public GrupoDetalle detalleGrupo(@PathVariable String id) {
		return adminService.detalleGrupo(id);
	}
}
