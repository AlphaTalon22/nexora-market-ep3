package cl.duoc.nexoramarket.ms_pedidos.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.ms_pedidos.model.ProductoCatalogo;
import cl.duoc.nexoramarket.ms_pedidos.repository.ProductoCatalogoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lógica del consumer {@code pedidos-catalogo}: guarda el último estado conocido
 * de cada producto (upsert por productoId).
 * <p>
 * Es idempotente por diseño: aplicar dos veces el mismo evento deja el mismo
 * estado. Además descarta eventos más antiguos que el último aplicado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ProductoCatalogoRepository catalogoRepository;
    private final ValidadorEventos validadorEventos;

    @Transactional
    public void aplicar(InventarioEvento evento) {
        validadorEventos.validar(evento);
        LocalDateTime ocurrido = evento.ocurridoEn() != null ? evento.ocurridoEn() : LocalDateTime.now();

        ProductoCatalogo actual = catalogoRepository.findById(evento.productoId()).orElse(null);
        if (actual != null && actual.getActualizadoEn().isAfter(ocurrido)) {
            log.info("Evento {} del producto {} es más antiguo que el estado local, se ignora",
                    evento.tipo(), evento.productoId());
            return;
        }

        ProductoCatalogo producto = actual != null ? actual : new ProductoCatalogo();
        producto.setProductoId(evento.productoId());
        producto.setNombre(evento.nombre() != null ? evento.nombre() : "Producto " + evento.productoId());
        producto.setPrecio(evento.precio());
        producto.setStock(evento.stockActual());
        producto.setActivo(evento.activo());
        producto.setActualizadoEn(ocurrido);
        catalogoRepository.save(producto);
        log.info("Catálogo local: {} producto {} precio={} stock={} activo={}", evento.tipo(),
                evento.productoId(), evento.precio(), evento.stockActual(), evento.activo());
    }
}
