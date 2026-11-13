package cl.duoc.nexoramarket.ms_pagos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del pedido es obligatorio")
    @Column(nullable = false)
    private Long pedidoId;

    @Column(nullable = false)
    private String clienteId; // oid de Entra ID

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false)
    private String metodoPago; // TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA

    @Column(nullable = false, length = 4)
    private String ultimos4;

    @Column(nullable = false)
    private String estado; // APROBADO, RECHAZADO

    @Column(nullable = false)
    private LocalDateTime fechaPago;
}
