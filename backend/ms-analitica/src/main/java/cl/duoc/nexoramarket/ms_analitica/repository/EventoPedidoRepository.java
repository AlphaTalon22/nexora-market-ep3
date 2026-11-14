package cl.duoc.nexoramarket.ms_analitica.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_analitica.model.EventoPedido;

@Repository
public interface EventoPedidoRepository extends JpaRepository<EventoPedido, Long> {

	long countByTipo(TipoEventoPedido tipo);

	@Query("select coalesce(sum(e.total), 0) from EventoPedido e where e.tipo = :tipo")
	Double sumarTotalPorTipo(@Param("tipo") TipoEventoPedido tipo);

	List<EventoPedido> findByTipoAndFechaAfterOrderByFechaAsc(TipoEventoPedido tipo, LocalDateTime desde);
}
