package cl.duoc.nexoramarket.ms_productos.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cl.duoc.nexoramarket.ms_productos.model.Producto;
import cl.duoc.nexoramarket.ms_productos.repository.ProductoRepository;
import cl.duoc.nexoramarket.ms_productos.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Carga el catálogo de ejemplo si la tabla está vacía y, siempre, publica el
 * estado del catálogo en nexora.inventario.eventos para los consumidores.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogoInicial implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    @Override
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            List<Producto> catalogo = List.of(
                producto("Audífonos Bluetooth NexaSound X", "Cancelación de ruido activa, 40 horas de batería y estuche de carga rápida.", 39990, 25, "Audio", "audifonos"),
                producto("Parlante Portátil BoomBox", "Sonido 360°, resistente al agua IPX7 y 12 horas de reproducción.", 24990, 18, "Audio", "parlante"),
                producto("Teclado Mecánico RGB", "Switches táctiles, retroiluminación RGB y reposamuñecas magnético.", 54990, 12, "Computación", "teclado"),
                producto("Mouse Inalámbrico Pro", "Sensor de 16.000 DPI, conexión dual y batería de larga duración.", 19990, 40, "Computación", "mouse"),
                producto("Monitor 27\" 4K UHD", "Panel IPS, HDR10, 144 Hz y altura ajustable para trabajo y gaming.", 249990, 6, "Computación", "monitor"),
                producto("Smartphone NexaPhone 12", "Pantalla AMOLED 6.5\", cámara de 108 MP y carga de 65 W.", 399990, 9, "Smartphones", "smartphone"),
                producto("Smartwatch NexaFit", "GPS integrado, monitor de salud y autonomía de 10 días.", 89990, 15, "Smartphones", "smartwatch"),
                producto("Cargador Rápido 65W GaN", "Tres puertos, carga simultánea y tamaño compacto de viaje.", 14990, 50, "Accesorios", "cargador"),
                producto("Hub USB-C 7 en 1", "HDMI 4K, USB 3.0, lector SD y carga PD de 100 W.", 22990, 30, "Accesorios", "hub"),
                producto("Silla Gamer ErgoPro", "Soporte lumbar, reclinación 160° y estructura de acero.", 149990, 8, "Gaming", "silla"),
                producto("Control Inalámbrico XPlay", "Gatillos analógicos, vibración háptica y batería de 20 horas.", 34990, 20, "Gaming", "control"),
                producto("Lámpara LED de Escritorio", "Tres tonos de luz, brazo ajustable y carga inalámbrica.", 12990, 35, "Hogar", "lampara")
            );
            productoRepository.saveAll(catalogo);
            log.info("Catálogo inicial cargado: {} productos", catalogo.size());
        }
        try {
            int publicados = productoService.publicarCatalogo();
            log.info("Catálogo publicado en Kafka: {} productos", publicados);
        } catch (RuntimeException e) {
            log.error("No se pudo publicar el catálogo en Kafka: {}", e.getMessage());
        }
    }

    private static Producto producto(String nombre, String descripcion, double precio,
                                     int stock, String categoria, String imagen) {
        return new Producto(null, nombre, descripcion, precio, stock, categoria,
                "/img/productos/" + imagen + ".svg", 5);
    }
}
