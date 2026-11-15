package cl.duoc.nexoramarket.ms_productos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_productos.model.Producto;
import jakarta.persistence.LockModeType;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Lee el producto bloqueando la fila (SELECT ... FOR UPDATE). El consumer
     * productos-stock corre con concurrency 3: si dos pedidos pagados descuentan
     * el mismo producto a la vez, el bloqueo evita perder una de las restas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdParaActualizar(@Param("id") Long id);
}
