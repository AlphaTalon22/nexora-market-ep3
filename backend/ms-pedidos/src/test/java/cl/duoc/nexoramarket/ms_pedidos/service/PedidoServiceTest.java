package cl.duoc.nexoramarket.ms_pedidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.ms_pedidos.dto.CrearPedidoRequest;
import cl.duoc.nexoramarket.ms_pedidos.dto.ItemPedidoRequest;
import cl.duoc.nexoramarket.ms_pedidos.exception.EstadoPedidoInvalidoException;
import cl.duoc.nexoramarket.ms_pedidos.exception.ProductoNoDisponibleException;
import cl.duoc.nexoramarket.ms_pedidos.model.Pedido;
import cl.duoc.nexoramarket.ms_pedidos.model.ProductoCatalogo;
import cl.duoc.nexoramarket.ms_pedidos.repository.PedidoRepository;
import cl.duoc.nexoramarket.ms_pedidos.repository.ProductoCatalogoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoCatalogoRepository catalogoRepository;

    @Mock
    private ApplicationEventPublisher eventos;

    @InjectMocks
    private PedidoService pedidoService;

    private ProductoCatalogo producto(Long id, double precio, int stock, boolean activo) {
        return new ProductoCatalogo(id, "Producto " + id, precio, stock, activo, LocalDateTime.now());
    }

    private CrearPedidoRequest request(ItemPedidoRequest... items) {
        return new CrearPedidoRequest(List.of(items));
    }

    @Test
    void crearCalculaElTotalConLosPreciosDelCatalogoYPublicaPedidoCreado() {
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, 19990.0, 40, true)));
        when(catalogoRepository.findById(2L)).thenReturn(Optional.of(producto(2L, 54990.0, 12, true)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(50L);
            return p;
        });

        Pedido pedido = pedidoService.crear("cliente-1",
                request(new ItemPedidoRequest(1L, 2), new ItemPedidoRequest(2L, 1)));

        assertThat(pedido.getTotal()).isEqualTo(19990.0 * 2 + 54990.0);
        assertThat(pedido.getEstado()).isEqualTo(Pedido.PENDIENTE);
        assertThat(pedido.getClienteId()).isEqualTo("cliente-1");
        assertThat(pedido.getItems()).hasSize(2);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventos).publishEvent(captor.capture());
        PedidoEvento evento = (PedidoEvento) captor.getValue();
        assertThat(evento.tipo()).isEqualTo(TipoEventoPedido.PEDIDO_CREADO);
        assertThat(evento.pedidoId()).isEqualTo(50L);
        assertThat(evento.items()).hasSize(2);
        assertThat(evento.total()).isEqualTo(pedido.getTotal());
    }

    @Test
    void productoQueNoEstaEnElCatalogoNoSePuedePedir() {
        when(catalogoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.crear("cliente-1", request(new ItemPedidoRequest(9L, 1))))
                .isInstanceOf(ProductoNoDisponibleException.class);
        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(eventos);
    }

    @Test
    void productoInactivoNoSePuedePedir() {
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, 19990.0, 40, false)));

        assertThatThrownBy(() -> pedidoService.crear("cliente-1", request(new ItemPedidoRequest(1L, 1))))
                .isInstanceOf(ProductoNoDisponibleException.class);
    }

    @Test
    void stockInsuficienteRechazaElPedido() {
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, 19990.0, 2, true)));

        assertThatThrownBy(() -> pedidoService.crear("cliente-1", request(new ItemPedidoRequest(1L, 5))))
                .isInstanceOf(ProductoNoDisponibleException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void cancelarUnPedidoPendientePublicaPedidoCancelado() {
        Pedido pendiente = new Pedido(7L, "cliente-1", LocalDateTime.now(), 19990.0, Pedido.PENDIENTE);
        when(pedidoRepository.findByIdAndClienteId(7L, "cliente-1")).thenReturn(Optional.of(pendiente));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido cancelado = pedidoService.cancelar(7L, "cliente-1");

        assertThat(cancelado.getEstado()).isEqualTo(Pedido.CANCELADO);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventos).publishEvent(captor.capture());
        assertThat(((PedidoEvento) captor.getValue()).tipo()).isEqualTo(TipoEventoPedido.PEDIDO_CANCELADO);
    }

    @Test
    void noSePuedeCancelarUnPedidoPagado() {
        Pedido pagado = new Pedido(7L, "cliente-1", LocalDateTime.now(), 19990.0, Pedido.PAGADO);
        when(pedidoRepository.findByIdAndClienteId(7L, "cliente-1")).thenReturn(Optional.of(pagado));

        assertThatThrownBy(() -> pedidoService.cancelar(7L, "cliente-1"))
                .isInstanceOf(EstadoPedidoInvalidoException.class);
        verifyNoInteractions(eventos);
    }
}
