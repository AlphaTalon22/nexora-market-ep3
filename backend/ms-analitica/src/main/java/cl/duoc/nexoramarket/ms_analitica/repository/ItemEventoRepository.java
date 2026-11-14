package cl.duoc.nexoramarket.ms_analitica.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_analitica.model.ItemEvento;

@Repository
public interface ItemEventoRepository extends JpaRepository<ItemEvento, Long> {

	@Query("""
			select i.productoId, sum(i.cantidad), sum(i.cantidad * i.precioUnitario)
			from ItemEvento i
			where i.evento.tipo = :tipo
			group by i.productoId
			order by sum(i.cantidad * i.precioUnitario) desc
			""")
	List<Object[]> agregarPorProducto(@Param("tipo") TipoEventoPedido tipo, Pageable pageable);
}
