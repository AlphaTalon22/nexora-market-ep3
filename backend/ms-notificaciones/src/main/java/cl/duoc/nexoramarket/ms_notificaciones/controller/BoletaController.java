package cl.duoc.nexoramarket.ms_notificaciones.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.nexoramarket.ms_notificaciones.boleta.BoletaService;
import cl.duoc.nexoramarket.ms_notificaciones.common.ClienteIdResolver;
import cl.duoc.nexoramarket.ms_notificaciones.model.Boleta;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boletas")
@RequiredArgsConstructor
public class BoletaController {

	private final BoletaService boletaService;

	@GetMapping
	public List<Boleta> listar(@AuthenticationPrincipal Jwt jwt) {
		return boletaService.listarPorCliente(ClienteIdResolver.desde(jwt));
	}

	@GetMapping("/pedido/{pedidoId}")
	public Boleta buscarPorPedido(@PathVariable Long pedidoId, @AuthenticationPrincipal Jwt jwt) {
		return boletaService.buscarPorPedido(ClienteIdResolver.desde(jwt), pedidoId);
	}
}
