package cl.duoc.nexoramarket.ms_pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_pedidos.model.ProductoCatalogo;

@Repository
public interface ProductoCatalogoRepository extends JpaRepository<ProductoCatalogo, Long> {
}
