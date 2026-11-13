package cl.duoc.nexoramarket.ms_notificaciones.boleta;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.ms_notificaciones.common.RecursoNoEncontradoException;
import cl.duoc.nexoramarket.ms_notificaciones.common.ValidadorMensajes;
import cl.duoc.nexoramarket.ms_notificaciones.messaging.contract.GenerarBoletaCommand;
import cl.duoc.nexoramarket.ms_notificaciones.model.Boleta;
import cl.duoc.nexoramarket.ms_notificaciones.repository.BoletaRepository;

/**
 * Lógica de negocio de las boletas: guarda una boleta en la BD por cada pago aprobado.
 * <p>
 * Es idempotente: si la boleta del pedido ya existe (mensaje duplicado), no la vuelve a guardar.
 */
@Service
public class BoletaService {

	private static final Logger log = LoggerFactory.getLogger(BoletaService.class);

	private final ValidadorMensajes validador;
	private final BoletaRepository boletaRepository;

	public BoletaService(ValidadorMensajes validador, BoletaRepository boletaRepository) {
		this.validador = validador;
		this.boletaRepository = boletaRepository;
	}

	/**
	 * Genera y guarda la boleta del pago.
	 *
	 * @return la boleta guardada (o la existente si el mensaje era duplicado)
	 * @throws cl.duoc.nexoramarket.ms_notificaciones.common.MensajeInvalidoException si el comando viene incompleto
	 */
	@Transactional
	public Boleta generar(GenerarBoletaCommand comando) {
		validador.validar(comando);

		return boletaRepository.findByPedidoId(comando.pedidoId())
				.map(existente -> {
					log.info("[BOLETA] La boleta del pedido {} ya existía (mensaje duplicado), no se duplica: id={}",
							comando.pedidoId(), existente.getId());
					return existente;
				})
				.orElseGet(() -> guardar(comando));
	}

	@Transactional(readOnly = true)
	public List<Boleta> listarPorCliente(String clienteId) {
		return boletaRepository.findByClienteIdOrderByFechaDesc(clienteId);
	}

	@Transactional(readOnly = true)
	public Boleta buscarPorPedido(String clienteId, Long pedidoId) {
		return boletaRepository.findByPedidoId(pedidoId)
				.filter(boleta -> clienteId.equals(boleta.getClienteId()))
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"No existe boleta para el pedido " + pedidoId));
	}

	private Boleta guardar(GenerarBoletaCommand comando) {
		LocalDateTime fecha = comando.fechaPago() != null ? comando.fechaPago() : LocalDateTime.now();
		Boleta boleta = new Boleta(null, comando.pagoId(), comando.pedidoId(), comando.clienteId(),
				comando.monto(), comando.metodoPago(), fecha);
		Boleta guardada = boletaRepository.save(boleta);
		log.info("[BOLETA] Boleta guardada para el pago {} (pedido {}, cliente {}): id={}",
				comando.pagoId(), comando.pedidoId(), comando.clienteId(), guardada.getId());
		return guardada;
	}
}
