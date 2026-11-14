package cl.duoc.nexoramarket.ms_analitica.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_analitica.model.EventoProcesado;

@Repository
public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, String> {

	List<EventoProcesado> findTop20ByOrderByProcesadoEnDesc();
}
