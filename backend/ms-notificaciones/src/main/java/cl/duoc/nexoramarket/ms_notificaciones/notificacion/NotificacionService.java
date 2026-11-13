package cl.duoc.nexoramarket.ms_notificaciones.notificacion;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.ms_notificaciones.common.ErrorTransitorioException;
import cl.duoc.nexoramarket.ms_notificaciones.common.ValidadorMensajes;
import cl.duoc.nexoramarket.ms_notificaciones.messaging.contract.EventoNegocio;
import cl.duoc.nexoramarket.ms_notificaciones.model.Notificacion;
import cl.duoc.nexoramarket.ms_notificaciones.repository.NotificacionRepository;

/**
 * Lógica de negocio de las notificaciones al cliente.
 * <p>
 * No sabe nada de RabbitMQ: recibe un evento ya convertido, construye el mensaje
 * y guarda la notificación en la BD para que el cliente pueda consultarla por REST.
 */
@Service
public class NotificacionService {

	private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
	private static final NumberFormat CLP = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"));

	private final ValidadorMensajes validador;
	private final NotificacionRepository notificacionRepository;
	private final boolean simularFalloCorreo;

	public NotificacionService(ValidadorMensajes validador, NotificacionRepository notificacionRepository,
			@Value("${nexora.notificaciones.simular-fallo-correo:false}") boolean simularFalloCorreo) {
		this.validador = validador;
		this.notificacionRepository = notificacionRepository;
		this.simularFalloCorreo = simularFalloCorreo;
	}

	/**
	 * Valida el evento, guarda la notificación y la "envía" al cliente.
	 *
	 * @return el texto enviado al cliente
	 * @throws cl.duoc.nexoramarket.ms_notificaciones.common.MensajeInvalidoException si el evento no trae los datos mínimos
	 * @throws ErrorTransitorioException si el servidor de correo no está disponible
	 */
	@Transactional
	public String notificar(EventoNegocio evento) {
		validador.validar(evento);

		if (simularFalloCorreo) {
			throw new ErrorTransitorioException("Servidor de correo no disponible (fallo simulado)");
		}

		String texto = construirMensaje(evento);
		Notificacion notificacion = new Notificacion(null, evento.clienteId(), tipoDe(evento.tipo()), texto,
				LocalDateTime.now(), false);
		notificacionRepository.save(notificacion);

		log.info("[NOTIFICACION] Registrada para el cliente {} -> {}",
				evento.clienteId() != null ? evento.clienteId() : "(sin cliente)",
				texto);
		return texto;
	}

	@Transactional(readOnly = true)
	public List<Notificacion> listarPorCliente(String clienteId) {
		return notificacionRepository.findByClienteIdOrderByFechaDesc(clienteId);
	}

	String construirMensaje(EventoNegocio evento) {
		return switch (evento.tipo()) {
			case "pedido.creado" -> "Recibimos tu pedido #" + evento.pedidoId()
					+ " por " + formatear(evento.monto()) + ". Estado: " + evento.estado() + ".";
			case "pago.aprobado" -> "Tu pago #" + evento.pagoId() + " del pedido #" + evento.pedidoId()
					+ " fue APROBADO (" + formatear(evento.monto()) + "). Pronto recibirás tu boleta.";
			case "pago.rechazado" -> "Tu pago del pedido #" + evento.pedidoId()
					+ " fue RECHAZADO. Puedes intentarlo con otro medio de pago.";
			default -> "Hay novedades en tu pedido #" + evento.pedidoId() + " (" + evento.tipo() + ").";
		};
	}

	private String tipoDe(String tipoEvento) {
		if (tipoEvento == null) {
			return "PEDIDO";
		}
		if (tipoEvento.startsWith("pago")) {
			return "PAGO";
		}
		if (tipoEvento.startsWith("boleta")) {
			return "BOLETA";
		}
		return "PEDIDO";
	}

	private String formatear(Double monto) {
		return monto == null ? "monto no informado" : CLP.format(monto);
	}
}
