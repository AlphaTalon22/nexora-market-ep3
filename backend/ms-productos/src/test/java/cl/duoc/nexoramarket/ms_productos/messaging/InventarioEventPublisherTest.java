package cl.duoc.nexoramarket.ms_productos.messaging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoInventario;
import cl.duoc.nexoramarket.common.kafka.Topicos;

@ExtendWith(MockitoExtension.class)
class InventarioEventPublisherTest {

    @Mock
    private KafkaTemplate<String, InventarioEvento> kafkaTemplate;

    @InjectMocks
    private InventarioEventPublisher publisher;

    @Test
    void publicaEnElTopicoDeInventarioConKeyProductoId() {
        InventarioEvento evento = new InventarioEvento("inv-1", TipoEventoInventario.PRODUCTO_CREADO,
                "ms-productos", LocalDateTime.now(), 7L, "SKU-7", "Mouse", "Computación", 19990.0,
                null, 10, 5, true, null);
        when(kafkaTemplate.send(Topicos.INVENTARIO, "7", evento)).thenReturn(new CompletableFuture<>());

        publisher.publicar(evento);

        verify(kafkaTemplate).send(Topicos.INVENTARIO, "7", evento);
    }
}
