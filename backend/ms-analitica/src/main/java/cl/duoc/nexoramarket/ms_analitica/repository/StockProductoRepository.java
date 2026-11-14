package cl.duoc.nexoramarket.ms_analitica.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_analitica.model.StockProducto;

@Repository
public interface StockProductoRepository extends JpaRepository<StockProducto, Long> {

	@Query("select s from StockProducto s where s.stockActual <= s.stockMinimo order by s.stockActual asc")
	List<StockProducto> buscarStockBajo();
}
