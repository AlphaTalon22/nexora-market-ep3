package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import java.util.List;

public record GrupoDetalle(
		String grupo,
		String estado,
		List<MiembroGrupo> miembros,
		List<LagParticion> lagPorParticion) {
}
