package cl.duoc.nexoramarket.ms_pedidos.service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_pedidos.exception.MensajeInvalidoException;
import cl.duoc.nexoramarket.ms_pedidos.exception.PedidoNoEncontradoException;
import cl.duoc.nexoramarket.ms_pedidos.messaging.event.EventoNegocio;
import cl.duoc.nexoramarket.ms_pedidos.messaging.kafka.PedidoEventos;
import cl.duoc.nexoramarket.ms_pedidos.model.Pedido;
import cl.duoc.nexoramarket.ms_pedidos.repository.PedidoRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Lógica de negocio: actualizar el estado de un pedido según el resultado de su
 * pago (llega por RabbitMQ) y avisarlo por Kafka con PEDIDO_PAGADO / PEDIDO_RECHAZADO.
 * No conoce RabbitMQ ni Kafka; recibe el evento ya convertido desde el consumidor.
 */
@Service
public class PedidoEstadoService {

	private static final Logger log = LoggerFactory.getLogger(PedidoEstadoService.class);

	/** Estado que toma el pedido según el tipo de evento de pago recibido. */
	static final Map<String, String> ESTADO_SEGUN_PAGO = Map.of(
			"pago.aprobado", Pedido.PAGADO,
			"pago.rechazado", Pedido.RECHAZADO);

	private final PedidoRepository pedidoRepository;
	private final Validator validator;
	private final ApplicationEventPublisher eventos;

	public PedidoEstadoService(PedidoRepository pedidoRepository, Validator validator,
			ApplicationEventPublisher eventos) {
		this.pedidoRepository = pedidoRepository;
		this.validator = validator;
		this.eventos = eventos;
	}

	/**
	 * @return el pedido con su nuevo estado
	 * @throws MensajeInvalidoException     si el evento viene incompleto o no es de pago
	 * @throws PedidoNoEncontradoException  si el pedido no existe
	 */
	@Transactional
	public Pedido aplicarResultadoPago(EventoNegocio evento) {
		validar(evento);

		String nuevoEstado = ESTADO_SEGUN_PAGO.get(evento.tipo());
		if (nuevoEstado == null) {
			throw new MensajeInvalidoException("Tipo de evento no soportado por ms-pedidos: " + evento.tipo());
		}

		Pedido pedido = pedidoRepository.findById(evento.pedidoId())
				.orElseThrow(() -> new PedidoNoEncontradoException(evento.pedidoId()));

		if (nuevoEstado.equals(pedido.getEstado())) {
			log.info("[PEDIDO] El pedido {} ya estaba en estado {} (evento repetido), sin cambios",
					pedido.getId(), nuevoEstado);
			return pedido;
		}

		String estadoAnterior = pedido.getEstado();
		pedido.setEstado(nuevoEstado);
		Pedido actualizado = pedidoRepository.save(pedido);
		log.info("[PEDIDO] Pedido {}: {} -> {} (pago {})", pedido.getId(), estadoAnterior, nuevoEstado,
				evento.pagoId());

		TipoEventoPedido tipo = Pedido.PAGADO.equals(nuevoEstado)
				? TipoEventoPedido.PEDIDO_PAGADO
				: TipoEventoPedido.PEDIDO_RECHAZADO;
		eventos.publishEvent(PedidoEventos.de(tipo, actualizado, evento.metodoPago()));
		return actualizado;
	}

	private void validar(EventoNegocio evento) {
		if (evento == null) {
			throw new MensajeInvalidoException("El evento llegó vacío");
		}
		Set<ConstraintViolation<EventoNegocio>> violaciones = validator.validate(evento);
		if (!violaciones.isEmpty()) {
			throw new MensajeInvalidoException("Evento de pago inválido: " + violaciones.stream()
					.map(v -> v.getPropertyPath() + " " + v.getMessage())
					.sorted()
					.collect(Collectors.joining(", ")));
		}
	}
}
