package cl.duoc.nexoramarket.ms_analitica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoInventario;
import cl.duoc.nexoramarket.ms_analitica.dto.AlertaStock;
import cl.duoc.nexoramarket.ms_analitica.model.StockProducto;
import cl.duoc.nexoramarket.ms_analitica.repository.EventoProcesadoRepository;
import cl.duoc.nexoramarket.ms_analitica.repository.StockProductoRepository;

@ExtendWith(MockitoExtension.class)
class InventarioAnalyticsServiceTest {

	@Mock
	private EventoProcesadoRepository eventoProcesadoRepository;

	@Mock
	private StockProductoRepository stockProductoRepository;

	@Mock
	private ValidadorEventos validadorEventos;

	@InjectMocks
	private InventarioAnalyticsService inventarioAnalyticsService;

	private InventarioEvento evento(String eventoId, int stockActual, int stockMinimo) {
		return new InventarioEvento(eventoId, TipoEventoInventario.STOCK_DESCONTADO, "ms-productos",
				LocalDateTime.now(), 7L, "SKU-1", "Mouse", "Perifericos", 5000.0, 10, stockActual,
				stockMinimo, true, 10L);
	}

	@Test
	void eventoDuplicadoNoActualizaElStock() {
		when(eventoProcesadoRepository.existsById("inv-1")).thenReturn(true);

		inventarioAnalyticsService.procesar(evento("inv-1", 5, 3), 0, 1L);

		verify(eventoProcesadoRepository, never()).save(any());
		verify(stockProductoRepository, never()).save(any());
	}

	@Test
	void eventoNuevoActualizaElStockDelProducto() {
		when(eventoProcesadoRepository.existsById("inv-2")).thenReturn(false);
		StockProducto existente = new StockProducto(7L, "Mouse", 10, 2);
		when(stockProductoRepository.findById(7L)).thenReturn(Optional.of(existente));

		inventarioAnalyticsService.procesar(evento("inv-2", 4, 2), 1, 9L);

		ArgumentCaptor<StockProducto> captor = ArgumentCaptor.forClass(StockProducto.class);
		verify(stockProductoRepository).save(captor.capture());
		assertThat(captor.getValue().getStockActual()).isEqualTo(4);
		assertThat(captor.getValue().getStockMinimo()).isEqualTo(2);
		assertThat(captor.getValue().getNombre()).isEqualTo("Mouse");
	}

	@Test
	void alertaCuandoElStockEstaEnElMinimo() {
		when(stockProductoRepository.buscarStockBajo())
				.thenReturn(List.of(new StockProducto(7L, "Mouse", 2, 2)));

		assertThat(inventarioAnalyticsService.alertas())
				.containsExactly(new AlertaStock(7L, "Mouse", 2));
	}
}
