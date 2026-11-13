package cl.duoc.nexoramarket.ms_notificaciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_notificaciones.model.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

	List<Notificacion> findByClienteIdOrderByFechaDesc(String clienteId);
}
