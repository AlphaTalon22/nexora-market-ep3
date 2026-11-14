package cl.duoc.nexoramarket.ms_analitica.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.nexoramarket.ms_analitica.dto.AlertaStock;
import cl.duoc.nexoramarket.ms_analitica.dto.EventoKafka;
import cl.duoc.nexoramarket.ms_analitica.dto.ResumenAnalitica;
import cl.duoc.nexoramarket.ms_analitica.dto.TopProducto;
import cl.duoc.nexoramarket.ms_analitica.dto.VentaPorMinuto;
import cl.duoc.nexoramarket.ms_analitica.service.InventarioAnalyticsService;
import cl.duoc.nexoramarket.ms_analitica.service.PedidoAnalyticsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analitica")
@RequiredArgsConstructor
public class AnaliticaController {

	private final PedidoAnalyticsService pedidoAnalyticsService;
	private final InventarioAnalyticsService inventarioAnalyticsService;

	@GetMapping("/resumen")
	public ResumenAnalitica resumen() {
		return pedidoAnalyticsService.resumen();
	}

	@GetMapping("/ventas-por-minuto")
	public List<VentaPorMinuto> ventasPorMinuto() {
		return pedidoAnalyticsService.ventasPorMinuto();
	}

	@GetMapping("/top-productos")
	public List<TopProducto> topProductos() {
		return pedidoAnalyticsService.topProductos();
	}

	@GetMapping("/eventos-recientes")
	public List<EventoKafka> eventosRecientes() {
		return pedidoAnalyticsService.eventosRecientes();
	}

	@GetMapping("/alertas")
	public List<AlertaStock> alertas() {
		return inventarioAnalyticsService.alertas();
	}
}
