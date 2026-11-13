package cl.duoc.nexoramarket.ms_notificaciones.common;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Obtiene el identificador del cliente desde el JWT de Entra ID:
 * claim {@code oid} y, si no viene, {@code sub}.
 */
public final class ClienteIdResolver {

	private ClienteIdResolver() {
	}

	public static String desde(Jwt jwt) {
		String oid = jwt.getClaimAsString("oid");
		return (oid != null && !oid.isBlank()) ? oid : jwt.getSubject();
	}
}
