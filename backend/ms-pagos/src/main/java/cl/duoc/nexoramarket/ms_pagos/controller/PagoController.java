package cl.duoc.nexoramarket.ms_pagos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import cl.duoc.nexoramarket.ms_pagos.dto.CrearPagoRequest;
import cl.duoc.nexoramarket.ms_pagos.model.Pago;
import cl.duoc.nexoramarket.ms_pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<Pago> procesar(@Valid @RequestBody CrearPagoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Pago pago = pagoService.procesar(request, clienteId(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(pago);
    }

    @GetMapping
    public List<Pago> listar(@AuthenticationPrincipal Jwt jwt) {
        return pagoService.listarPorCliente(clienteId(jwt));
    }

    @GetMapping("/pedido/{pedidoId}")
    public Pago buscarPorPedido(@PathVariable Long pedidoId, @AuthenticationPrincipal Jwt jwt) {
        return pagoService.buscarPorPedido(clienteId(jwt), pedidoId);
    }

    private String clienteId(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        return (oid != null && !oid.isBlank()) ? oid : jwt.getSubject();
    }
}
