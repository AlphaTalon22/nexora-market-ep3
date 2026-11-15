package cl.duoc.nexoramarket.ms_pedidos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.nexoramarket.ms_pedidos.common.ClienteIdResolver;
import cl.duoc.nexoramarket.ms_pedidos.dto.CrearPedidoRequest;
import cl.duoc.nexoramarket.ms_pedidos.dto.PedidoResponse;
import cl.duoc.nexoramarket.ms_pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API de pedidos. El cliente se identifica siempre por el JWT (claim oid/sub):
 * cada usuario solo ve y modifica sus propios pedidos.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return pedidoService.listar(ClienteIdResolver.desde(jwt)).stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return PedidoResponse.desde(pedidoService.obtener(id, ClienteIdResolver.desde(jwt)));
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody CrearPedidoRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PedidoResponse.desde(pedidoService.crear(ClienteIdResolver.desde(jwt), request)));
    }

    @PostMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return PedidoResponse.desde(pedidoService.cancelar(id, ClienteIdResolver.desde(jwt)));
    }
}
