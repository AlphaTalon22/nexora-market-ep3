package cl.duoc.nexoramarket.ms_productos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoInventario;
import cl.duoc.nexoramarket.ms_productos.model.Producto;
import cl.duoc.nexoramarket.ms_productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ApplicationEventPublisher eventos;

    @InjectMocks
    private ProductoService productoService;

    private InventarioEvento eventoPublicado() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventos).publishEvent(captor.capture());
        return (InventarioEvento) captor.getValue();
    }

    @Test
    void crearAsignaStockMinimoYPublicaProductoCreado() {
        Producto nuevo = new Producto(null, "Mouse", "Gamer", 19990.0, 10, "Computación",
                "/img/productos/mouse.svg", null);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Producto guardado = productoService.crear(nuevo);

        assertThat(guardado.getStockMinimo()).isEqualTo(5);
        InventarioEvento evento = eventoPublicado();
        assertThat(evento.tipo()).isEqualTo(TipoEventoInventario.PRODUCTO_CREADO);
        assertThat(evento.productoId()).isEqualTo(1L);
        assertThat(evento.stockActual()).isEqualTo(10);
        assertThat(evento.origen()).isEqualTo("ms-productos");
    }

    @Test
    void actualizarPublicaProductoActualizadoConStockAnterior() {
        Producto existente = new Producto(1L, "Mouse", "Gamer", 19990.0, 10, "Computación",
                "/img/productos/mouse.svg", 5);
        Producto datos = new Producto(null, "Mouse Pro", "Gamer", 21990.0, 7, "Computación",
                "/img/productos/mouse.svg", null);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Producto> resultado = productoService.actualizar(1L, datos);

        assertThat(resultado).isPresent();
        InventarioEvento evento = eventoPublicado();
        assertThat(evento.tipo()).isEqualTo(TipoEventoInventario.PRODUCTO_ACTUALIZADO);
        assertThat(evento.stockAnterior()).isEqualTo(10);
        assertThat(evento.stockActual()).isEqualTo(7);
        assertThat(evento.precio()).isEqualTo(21990.0);
    }
}
