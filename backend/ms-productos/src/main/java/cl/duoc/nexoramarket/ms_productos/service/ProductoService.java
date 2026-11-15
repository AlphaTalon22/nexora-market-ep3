package cl.duoc.nexoramarket.ms_productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.nexoramarket.ms_productos.model.Producto;
import cl.duoc.nexoramarket.ms_productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final int STOCK_MINIMO_POR_DEFECTO = 5;

    private final ProductoRepository productoRepository;

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
        return productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> actualizar(Long id, Producto datos) {
        return productoRepository.findById(id).map(producto -> {
            producto.setNombre(datos.getNombre());
            producto.setDescripcion(datos.getDescripcion());
            producto.setPrecio(datos.getPrecio());
            producto.setStock(datos.getStock());
            producto.setCategoria(datos.getCategoria());
            producto.setImagen(datos.getImagen());
            if (datos.getStockMinimo() != null) {
                producto.setStockMinimo(datos.getStockMinimo());
            }
            return productoRepository.save(producto);
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            return false;
        }
        productoRepository.deleteById(id);
        return true;
    }
}
