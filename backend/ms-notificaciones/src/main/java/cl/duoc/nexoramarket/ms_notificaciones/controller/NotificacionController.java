package cl.duoc.nexoramarket.ms_notificaciones.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.nexoramarket.ms_notificaciones.common.ClienteIdResolver;
import cl.duoc.nexoramarket.ms_notificaciones.model.Notificacion;
import cl.duoc.nexoramarket.ms_notificaciones.notificacion.NotificacionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

	private final NotificacionService notificacionService;

	@GetMapping
	public List<Notificacion> listar(@AuthenticationPrincipal Jwt jwt) {
		return notificacionService.listarPorCliente(ClienteIdResolver.desde(jwt));
	}
}
