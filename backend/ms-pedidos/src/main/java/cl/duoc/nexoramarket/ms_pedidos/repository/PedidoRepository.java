package cl.duoc.nexoramarket.ms_pedidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_pedidos.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteIdOrderByFechaDesc(String clienteId);

    Optional<Pedido> findByIdAndClienteId(Long id, String clienteId);
}
