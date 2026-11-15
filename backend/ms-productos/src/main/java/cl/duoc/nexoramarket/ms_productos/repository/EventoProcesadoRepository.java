package cl.duoc.nexoramarket.ms_productos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_productos.model.EventoProcesado;

@Repository
public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, String> {
}
