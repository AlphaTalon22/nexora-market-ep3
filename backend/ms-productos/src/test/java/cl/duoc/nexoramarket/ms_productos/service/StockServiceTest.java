package cl.duoc.nexoramarket.ms_productos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.context.ApplicationEventPublisher;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.ItemPedidoEvento;
import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoInventario;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_productos.model.Producto;
import cl.duoc.nexoramarket.ms_productos.repository.EventoProcesadoRepository;
import cl.duoc.nexoramarket.ms_productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private EventoProcesadoRepository eventoProcesadoRepository;

    @Mock
    private ValidadorEventos validadorEventos;

    @Mock
    private ApplicationEventPublisher eventos;

    @InjectMocks
    private StockService stockService;

    private PedidoEvento evento(String eventoId, TipoEventoPedido tipo, int cantidad) {
        return new PedidoEvento(eventoId, tipo, "ms-pedidos", LocalDateTime.now(), 100L, "cliente-1",
                List.of(new ItemPedidoEvento(1L, cantidad, 19990.0, 19990.0 * cantidad)),
                19990.0 * cantidad, "PAGADO", "TARJETA");
    }

    private Producto mouse(int stock) {
        return new Producto(1L, "Mouse", "Gamer", 19990.0, stock, "Computación", "/img/productos/mouse.svg", 5);
    }

    @Test
    void ignoraLosEventosQueNoSonPago() {
        stockService.procesar(evento("e-1", TipoEventoPedido.PEDIDO_CREADO, 2), 0, 1L);

        verifyNoInteractions(productoRepository, eventoProcesadoRepository, eventos);
    }

    @Test
    void eventoDuplicadoNoDescuentaDosVeces() {
        when(eventoProcesadoRepository.existsById("e-2")).thenReturn(true);

        stockService.procesar(evento("e-2", TipoEventoPedido.PEDIDO_PAGADO, 2), 0, 2L);

        verify(eventoProcesadoRepository, never()).save(any());
        verifyNoInteractions(productoRepository, eventos);
    }

    @Test
    void pedidoPagadoDescuentaStockYPublicaStockDescontado() {
        when(eventoProcesadoRepository.existsById("e-3")).thenReturn(false);
        when(productoRepository.findByIdParaActualizar(1L)).thenReturn(Optional.of(mouse(10)));

        stockService.procesar(evento("e-3", TipoEventoPedido.PEDIDO_PAGADO, 3), 1, 7L);

        ArgumentCaptor<Producto> guardado = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(guardado.capture());
        assertThat(guardado.getValue().getStock()).isEqualTo(7);

        ArgumentCaptor<Object> publicado = ArgumentCaptor.forClass(Object.class);
        verify(eventos).publishEvent(publicado.capture());
        InventarioEvento inventario = (InventarioEvento) publicado.getValue();
        assertThat(inventario.tipo()).isEqualTo(TipoEventoInventario.STOCK_DESCONTADO);
        assertThat(inventario.stockAnterior()).isEqualTo(10);
        assertThat(inventario.stockActual()).isEqualTo(7);
        assertThat(inventario.pedidoId()).isEqualTo(100L);
    }

    @Test
    void stockInsuficienteQuedaEnCero() {
        when(eventoProcesadoRepository.existsById("e-4")).thenReturn(false);
        when(productoRepository.findByIdParaActualizar(1L)).thenReturn(Optional.of(mouse(2)));

        stockService.procesar(evento("e-4", TipoEventoPedido.PEDIDO_PAGADO, 5), 2, 9L);

        ArgumentCaptor<Producto> guardado = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(guardado.capture());
        assertThat(guardado.getValue().getStock()).isZero();
    }
}
