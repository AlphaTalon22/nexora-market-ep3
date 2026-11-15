package cl.duoc.nexoramarket.ms_pedidos.messaging.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import cl.duoc.nexoramarket.ms_pedidos.service.CatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Consumer group {@code pedidos-catalogo}: mantiene la copia local de precios y
 * stock a partir de {@code nexora.inventario.eventos}. Tres hilos (uno por partición).
 */
@Component
@RequiredArgsConstructor
public class InventarioEventListener {

    private final CatalogoService catalogoService;

    @KafkaListener(topics = Topicos.INVENTARIO, groupId = "pedidos-catalogo",
            containerFactory = "inventarioListenerContainerFactory", concurrency = "3")
    public void escuchar(@Valid InventarioEvento evento) {
        catalogoService.aplicar(evento);
    }
}
