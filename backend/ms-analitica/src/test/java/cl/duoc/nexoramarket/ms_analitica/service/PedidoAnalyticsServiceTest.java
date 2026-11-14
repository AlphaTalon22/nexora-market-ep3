package cl.duoc.nexoramarket.ms_analitica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import cl.duoc.nexoramarket.common.kafka.ItemPedidoEvento;
import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.common.kafka.Topicos;
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

@ExtendWith(MockitoExtension.class)
class PedidoAnalyticsServiceTest {

	private static final DateTimeFormatter FORMATO_MINUTO = DateTimeFormatter.ofPattern("HH:mm");

	@Mock
	private EventoProcesadoRepository eventoProcesadoRepository;

	@Mock
	private EventoPedidoRepository eventoPedidoRepository;

	@Mock
	private ItemEventoRepository itemEventoRepository;

	@Mock
	private StockProductoRepository stockProductoRepository;

	@Mock
	private ValidadorEventos validadorEventos;

	@InjectMocks
	private PedidoAnalyticsService pedidoAnalyticsService;

	private PedidoEvento evento(TipoEventoPedido tipo, String eventoId) {
		return new PedidoEvento(eventoId, tipo, "ms-pedidos", LocalDateTime.now(), 10L, "oid-1",
				List.of(new ItemPedidoEvento(1L, 2, 5000.0, 10000.0),
						new ItemPedidoEvento(2L, 1, 3000.0, 3000.0)),
				13000.0, "PENDIENTE", "TARJETA_CREDITO");
	}

	@Test
	void eventoDuplicadoNoCuentaDosVeces() {
		when(eventoProcesadoRepository.existsById("evt-1")).thenReturn(true);

		pedidoAnalyticsService.procesar(evento(TipoEventoPedido.PEDIDO_CREADO, "evt-1"), 0, 15L);

		verify(eventoProcesadoRepository, never()).save(any());
		verify(eventoPedidoRepository, never()).save(any());
		verify(itemEventoRepository, never()).saveAll(any());
	}

	@Test
	void eventoNuevoGuardaCabeceraYUnaFilaPorItem() {
		when(eventoProcesadoRepository.existsById("evt-2")).thenReturn(false);

		pedidoAnalyticsService.procesar(evento(TipoEventoPedido.PEDIDO_PAGADO, "evt-2"), 2, 42L);

		ArgumentCaptor<EventoProcesado> procesado = ArgumentCaptor.forClass(EventoProcesado.class);
		verify(eventoProcesadoRepository).save(procesado.capture());
		assertThat(procesado.getValue().getTopico()).isEqualTo(Topicos.PEDIDOS);
		assertThat(procesado.getValue().getTipo()).isEqualTo("PEDIDO_PAGADO");
		assertThat(procesado.getValue().getParticion()).isEqualTo(2);
		assertThat(procesado.getValue().getOffset()).isEqualTo(42L);

		ArgumentCaptor<EventoPedido> pedido = ArgumentCaptor.forClass(EventoPedido.class);
		verify(eventoPedidoRepository).save(pedido.capture());
		assertThat(pedido.getValue().getTipo()).isEqualTo(TipoEventoPedido.PEDIDO_PAGADO);
		assertThat(pedido.getValue().getPedidoId()).isEqualTo(10L);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<ItemEvento>> items = ArgumentCaptor.forClass(List.class);
		verify(itemEventoRepository).saveAll(items.capture());
		assertThat(items.getValue()).hasSize(2);
	}

	@Test
	void resumenCuentaPedidosCreadosYVentasSoloPagadas() {
		when(eventoPedidoRepository.countByTipo(TipoEventoPedido.PEDIDO_CREADO)).thenReturn(5L);
		when(eventoPedidoRepository.countByTipo(TipoEventoPedido.PEDIDO_PAGADO)).thenReturn(3L);
		when(eventoPedidoRepository.sumarTotalPorTipo(TipoEventoPedido.PEDIDO_PAGADO)).thenReturn(39000.0);

		ResumenAnalitica resumen = pedidoAnalyticsService.resumen();

		assertThat(resumen.pedidos()).isEqualTo(5);
		assertThat(resumen.ventas()).isEqualTo(3);
		assertThat(resumen.ingreso()).isEqualTo(39000.0);
	}

	@Test
	void ventasPorMinutoDevuelveTreintaPuntosYRellenaConCero() {
		LocalDateTime haceDosMinutos = LocalDateTime.now().minusMinutes(2).withSecond(0).withNano(0);
		EventoPedido pago = new EventoPedido(1L, "evt-p", TipoEventoPedido.PEDIDO_PAGADO, 10L,
				"oid-1", 13000.0, haceDosMinutos);
		when(eventoPedidoRepository.findByTipoAndFechaAfterOrderByFechaAsc(
				eq(TipoEventoPedido.PEDIDO_PAGADO), any())).thenReturn(List.of(pago));

		List<VentaPorMinuto> serie = pedidoAnalyticsService.ventasPorMinuto();

		assertThat(serie).hasSize(30);
		assertThat(serie.stream().mapToLong(VentaPorMinuto::cantidad).sum()).isEqualTo(1);
		VentaPorMinuto punto = serie.stream()
				.filter(p -> p.minuto().equals(haceDosMinutos.format(FORMATO_MINUTO)))
				.findFirst()
				.orElseThrow();
		assertThat(punto.cantidad()).isEqualTo(1);
		assertThat(punto.monto()).isEqualTo(13000.0);
	}

	@Test
	void topProductosSoloConsultaPedidosPagadosYUsaElNombreDelStock() {
		when(itemEventoRepository.agregarPorProducto(eq(TipoEventoPedido.PEDIDO_PAGADO), any(Pageable.class)))
				.thenReturn(List.<Object[]>of(new Object[] { 1L, 5L, 25000.0 }));
		when(stockProductoRepository.findAllById(List.of(1L)))
				.thenReturn(List.of(new StockProducto(1L, "Teclado", 3, 1)));

		List<TopProducto> top = pedidoAnalyticsService.topProductos();

		assertThat(top).containsExactly(new TopProducto(1L, "Teclado", 5L, 25000.0));
		verify(itemEventoRepository)
				.agregarPorProducto(eq(TipoEventoPedido.PEDIDO_PAGADO), any(Pageable.class));
	}
}
