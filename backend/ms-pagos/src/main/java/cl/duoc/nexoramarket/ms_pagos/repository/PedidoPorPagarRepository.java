package cl.duoc.nexoramarket.ms_pagos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.nexoramarket.ms_pagos.model.PedidoPorPagar;

@Repository
public interface PedidoPorPagarRepository extends JpaRepository<PedidoPorPagar, Long> {

	Optional<PedidoPorPagar> findByPedidoId(Long pedidoId);
}
