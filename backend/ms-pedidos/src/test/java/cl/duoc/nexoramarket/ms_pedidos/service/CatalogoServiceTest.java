package cl.duoc.nexoramarket.ms_pedidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoInventario;
import cl.duoc.nexoramarket.ms_pedidos.model.ProductoCatalogo;
import cl.duoc.nexoramarket.ms_pedidos.repository.ProductoCatalogoRepository;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private ProductoCatalogoRepository catalogoRepository;

    @Mock
    private ValidadorEventos validadorEventos;

    @InjectMocks
    private CatalogoService catalogoService;

    private InventarioEvento evento(LocalDateTime ocurrido, int stock, boolean activo) {
        return new InventarioEvento("inv-1", TipoEventoInventario.STOCK_DESCONTADO, "ms-productos", ocurrido,
                1L, "SKU-1", "Mouse", "Computación", 19990.0, 10, stock, 5, activo, 100L);
    }

    @Test
    void productoNuevoSeAgregaAlCatalogoLocal() {
        when(catalogoRepository.findById(1L)).thenReturn(Optional.empty());

        catalogoService.aplicar(evento(LocalDateTime.now(), 7, true));

        ArgumentCaptor<ProductoCatalogo> captor = ArgumentCaptor.forClass(ProductoCatalogo.class);
        verify(catalogoRepository).save(captor.capture());
        assertThat(captor.getValue().getPrecio()).isEqualTo(19990.0);
        assertThat(captor.getValue().getStock()).isEqualTo(7);
        assertThat(captor.getValue().isActivo()).isTrue();
    }

    @Test
    void eventoMasAntiguoQueElEstadoLocalSeIgnora() {
        LocalDateTime ahora = LocalDateTime.now();
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(
                new ProductoCatalogo(1L, "Mouse", 19990.0, 3, true, ahora)));

        catalogoService.aplicar(evento(ahora.minusMinutes(5), 9, true));

        verify(catalogoRepository, never()).save(any());
    }

    @Test
    void productoEliminadoQuedaInactivo() {
        LocalDateTime antes = LocalDateTime.now().minusMinutes(5);
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(
                new ProductoCatalogo(1L, "Mouse", 19990.0, 3, true, antes)));

        catalogoService.aplicar(evento(LocalDateTime.now(), 3, false));

        ArgumentCaptor<ProductoCatalogo> captor = ArgumentCaptor.forClass(ProductoCatalogo.class);
        verify(catalogoRepository).save(captor.capture());
        assertThat(captor.getValue().isActivo()).isFalse();
    }
}
