package cl.duoc.nexoramarket.ms_analitica.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import cl.duoc.nexoramarket.ms_analitica.dto.EventoKafka;
import cl.duoc.nexoramarket.ms_analitica.dto.ResumenAnalitica;
import cl.duoc.nexoramarket.ms_analitica.dto.TopProducto;
import cl.duoc.nexoramarket.ms_analitica.dto.VentaPorMinuto;
import cl.duoc.nexoramarket.ms_analitica.model.EventoPedido;
import cl.duoc.nexoramarket.ms_analitica.model.EventoProcesado;
import cl.duoc.nexoramarket.ms_analitica.model.ItemEvento;
import cl.duoc.nexoramarket.ms_analitica.model.StockProducto;
import cl.duoc.nexoramarket.ms_analitica.repository.EventoPedidoRepository;
import cl.duoc.nexoramarket.ms_analitica.repository.EventoProcesadoRepository;
import cl.duoc.nexoramarket.ms_analitica.repository.ItemEventoRepository;
import cl.duoc.nexoramarket.ms_analitica.repository.StockProductoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoAnalyticsService {

	private static final DateTimeFormatter FORMATO_MINUTO = DateTimeFormatter.ofPattern("HH:mm");
	private static final int VENTANA_MINUTOS = 30;
	private static final int TOP_PRODUCTOS = 5;

	private final EventoProcesadoRepository eventoProcesadoRepository;
	private final EventoPedidoRepository eventoPedidoRepository;
	private final ItemEventoRepository itemEventoRepository;
	private final StockProductoRepository stockProductoRepository;
	private final ValidadorEventos validadorEventos;

	/**
	 * Procesa un evento de pedido de forma idempotente: si el eventoId ya fue
	 * procesado no vuelve a guardar nada, y si es nuevo persiste la cabecera y
	 * una fila por ítem.
	 */
	@Transactional
	public void procesar(PedidoEvento evento, int particion, long offset) {
		validadorEventos.validar(evento);
		if (eventoProcesadoRepository.existsById(evento.eventoId())) {
			return;
		}
		eventoProcesadoRepository.save(new EventoProcesado(evento.eventoId(), Topicos.PEDIDOS,
				evento.tipo().name(), particion, offset, LocalDateTime.now()));

		EventoPedido eventoPedido = new EventoPedido(null, evento.eventoId(), evento.tipo(),
				evento.pedidoId(), evento.clienteId(), evento.total(),
				evento.ocurridoEn() != null ? evento.ocurridoEn() : LocalDateTime.now());
		eventoPedidoRepository.save(eventoPedido);

		List<ItemEvento> items = evento.items().stream()
				.map(item -> new ItemEvento(null, eventoPedido, item.productoId(), item.cantidad(),
						item.precioUnitario()))
				.toList();
		itemEventoRepository.saveAll(items);
	}

	@Transactional(readOnly = true)
	public ResumenAnalitica resumen() {
		long pedidos = eventoPedidoRepository.countByTipo(TipoEventoPedido.PEDIDO_CREADO);
		long ventas = eventoPedidoRepository.countByTipo(TipoEventoPedido.PEDIDO_PAGADO);
		double ingreso = Optional
				.ofNullable(eventoPedidoRepository.sumarTotalPorTipo(TipoEventoPedido.PEDIDO_PAGADO))
				.orElse(0.0);
		return new ResumenAnalitica(pedidos, ventas, ingreso);
	}

	@Transactional(readOnly = true)
	public List<VentaPorMinuto> ventasPorMinuto() {
		LocalDateTime ahora = LocalDateTime.now();
		LocalDateTime desde = ahora.minusMinutes(VENTANA_MINUTOS).withSecond(0).withNano(0);

		Map<LocalDateTime, VentaPorMinuto> porMinuto = new LinkedHashMap<>();
		for (int i = VENTANA_MINUTOS - 1; i >= 0; i--) {
			LocalDateTime minuto = ahora.minusMinutes(i).withSecond(0).withNano(0);
			porMinuto.put(minuto, new VentaPorMinuto(minuto.format(FORMATO_MINUTO), 0, 0.0));
		}

		for (EventoPedido evento : eventoPedidoRepository
				.findByTipoAndFechaAfterOrderByFechaAsc(TipoEventoPedido.PEDIDO_PAGADO, desde)) {
			LocalDateTime minuto = evento.getFecha().withSecond(0).withNano(0);
			VentaPorMinuto actual = porMinuto.get(minuto);
			if (actual != null) {
				porMinuto.put(minuto, new VentaPorMinuto(actual.minuto(), actual.cantidad() + 1,
						actual.monto() + (evento.getTotal() != null ? evento.getTotal() : 0.0)));
			}
		}

		return new ArrayList<>(porMinuto.values());
	}

	@Transactional(readOnly = true)
	public List<TopProducto> topProductos() {
		List<Object[]> filas = itemEventoRepository.agregarPorProducto(TipoEventoPedido.PEDIDO_PAGADO,
				PageRequest.of(0, TOP_PRODUCTOS));

		List<Long> ids = filas.stream().map(fila -> (Long) fila[0]).toList();
		Map<Long, String> nombres = stockProductoRepository.findAllById(ids).stream()
				.collect(Collectors.toMap(StockProducto::getProductoId, StockProducto::getNombre));

		return filas.stream()
				.map(fila -> {
					Long productoId = (Long) fila[0];
					return new TopProducto(productoId, nombres.getOrDefault(productoId, "Producto " + productoId),
							((Number) fila[1]).longValue(), ((Number) fila[2]).doubleValue());
				})
				.toList();
	}

	@Transactional(readOnly = true)
	public List<EventoKafka> eventosRecientes() {
		return eventoProcesadoRepository.findTop20ByOrderByProcesadoEnDesc().stream()
				.map(evento -> new EventoKafka(evento.getTopico(), evento.getParticion(), evento.getOffset(),
						evento.getTipo(), evento.getProcesadoEn()))
				.toList();
	}
}
