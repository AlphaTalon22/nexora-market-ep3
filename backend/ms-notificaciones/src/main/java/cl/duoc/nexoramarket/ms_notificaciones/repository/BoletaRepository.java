package cl.duoc.nexoramarket.ms_notificaciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_notificaciones.model.Boleta;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

	Optional<Boleta> findByPedidoId(Long pedidoId);

	List<Boleta> findByClienteIdOrderByFechaDesc(String clienteId);
}
