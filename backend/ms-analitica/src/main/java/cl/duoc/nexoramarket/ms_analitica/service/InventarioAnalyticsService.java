package cl.duoc.nexoramarket.ms_analitica.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import cl.duoc.nexoramarket.ms_analitica.dto.AlertaStock;
import cl.duoc.nexoramarket.ms_analitica.model.EventoProcesado;
import cl.duoc.nexoramarket.ms_analitica.model.StockProducto;
import cl.duoc.nexoramarket.ms_analitica.repository.EventoProcesadoRepository;
import cl.duoc.nexoramarket.ms_analitica.repository.StockProductoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioAnalyticsService {

	private final EventoProcesadoRepository eventoProcesadoRepository;
	private final StockProductoRepository stockProductoRepository;
	private final ValidadorEventos validadorEventos;

	/**
	 * Procesa un evento de inventario de forma idempotente y actualiza el estado
	 * del producto para calcular alertas de stock bajo.
	 */
	@Transactional
	public void procesar(InventarioEvento evento, int particion, long offset) {
		validadorEventos.validar(evento);
		if (eventoProcesadoRepository.existsById(evento.eventoId())) {
			return;
		}
		eventoProcesadoRepository.save(new EventoProcesado(evento.eventoId(), Topicos.INVENTARIO,
				evento.tipo().name(), particion, offset, LocalDateTime.now()));

		StockProducto stock = stockProductoRepository.findById(evento.productoId())
				.orElseGet(() -> {
					StockProducto nuevo = new StockProducto();
					nuevo.setProductoId(evento.productoId());
					nuevo.setNombre("Producto " + evento.productoId());
					nuevo.setStockActual(0);
					nuevo.setStockMinimo(0);
					return nuevo;
				});

		if (evento.nombre() != null && !evento.nombre().isBlank()) {
			stock.setNombre(evento.nombre());
		}
		if (evento.stockMinimo() != null) {
			stock.setStockMinimo(evento.stockMinimo());
		}
		stock.setStockActual(evento.stockActual());
		stockProductoRepository.save(stock);
	}

	@Transactional(readOnly = true)
	public List<AlertaStock> alertas() {
		return stockProductoRepository.buscarStockBajo().stream()
				.map(stock -> new AlertaStock(stock.getProductoId(), stock.getNombre(), stock.getStockActual()))
				.toList();
	}
}
