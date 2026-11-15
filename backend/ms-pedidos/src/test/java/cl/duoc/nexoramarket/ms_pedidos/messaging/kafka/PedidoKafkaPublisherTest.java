package cl.duoc.nexoramarket.ms_pedidos.messaging.kafka;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import cl.duoc.nexoramarket.common.kafka.ItemPedidoEvento;
import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.TipoEventoPedido;
import cl.duoc.nexoramarket.common.kafka.Topicos;

@ExtendWith(MockitoExtension.class)
class PedidoKafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, PedidoEvento> kafkaTemplate;

    @InjectMocks
    private PedidoKafkaPublisher publisher;

    @Test
    void publicaEnElTopicoDePedidosConKeyPedidoId() {
        PedidoEvento evento = new PedidoEvento("ped-1", TipoEventoPedido.PEDIDO_CREADO, "ms-pedidos",
                LocalDateTime.now(), 42L, "cliente-1", List.of(new ItemPedidoEvento(1L, 2, 19990.0, 39980.0)),
                39980.0, "PENDIENTE", null);
        when(kafkaTemplate.send(Topicos.PEDIDOS, "42", evento)).thenReturn(new CompletableFuture<>());

        publisher.publicar(evento);

        verify(kafkaTemplate).send(Topicos.PEDIDOS, "42", evento);
    }
}
