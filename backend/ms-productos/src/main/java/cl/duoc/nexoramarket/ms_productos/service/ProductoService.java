package cl.duoc.nexoramarket.ms_productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.common.kafka.TipoEventoInventario;
import cl.duoc.nexoramarket.ms_productos.messaging.InventarioEventos;
import cl.duoc.nexoramarket.ms_productos.model.Producto;
import cl.duoc.nexoramarket.ms_productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

/**
 * Lógica del catálogo. Cada cambio publica un evento interno que
 * InventarioEventPublisher envía a Kafka después del commit.
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final int STOCK_MINIMO_POR_DEFECTO = 5;

    private final ProductoRepository productoRepository;
    private final ApplicationEventPublisher eventos;

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscar(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto crear(Producto producto) {
        producto.setId(null); // el id lo genera la BD, nunca el cliente
        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(STOCK_MINIMO_POR_DEFECTO);
        }
        Producto guardado = productoRepository.save(producto);
        eventos.publishEvent(InventarioEventos.de(TipoEventoInventario.PRODUCTO_CREADO,
                guardado, null, null));
        return guardado;
    }

    @Transactional
    public Optional<Producto> actualizar(Long id, Producto datos) {
        return productoRepository.findById(id).map(producto -> {
            Integer stockAnterior = producto.getStock();
            producto.setNombre(datos.getNombre());
            producto.setDescripcion(datos.getDescripcion());
            producto.setPrecio(datos.getPrecio());
            producto.setStock(datos.getStock());
            producto.setCategoria(datos.getCategoria());
            producto.setImagen(datos.getImagen());
            if (datos.getStockMinimo() != null) {
                producto.setStockMinimo(datos.getStockMinimo());
            }
            Producto guardado = productoRepository.save(producto);
            eventos.publishEvent(InventarioEventos.de(TipoEventoInventario.PRODUCTO_ACTUALIZADO,
                    guardado, stockAnterior, null));
            return guardado;
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            return false;
        }
        // Se avisa con activo=false para que los consumidores lo saquen de su catálogo
        eventos.publishEvent(InventarioEventos.de(TipoEventoInventario.PRODUCTO_ACTUALIZADO,
                producto.get(), producto.get().getStock(), null, false));
        productoRepository.delete(producto.get());
        return true;
    }

    /**
     * Publica el estado actual de todo el catálogo. Se llama al arrancar para que
     * ms-pedidos y ms-analitica tengan precios y stock aunque el tópico esté vacío.
     */
    @Transactional(readOnly = true)
    public int publicarCatalogo() {
        List<Producto> productos = productoRepository.findAll();
        productos.forEach(p -> eventos.publishEvent(InventarioEventos.de(
                TipoEventoInventario.PRODUCTO_ACTUALIZADO, p, p.getStock(), null)));
        return productos.size();
    }
}
