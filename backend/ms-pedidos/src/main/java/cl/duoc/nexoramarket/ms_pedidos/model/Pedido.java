package cl.duoc.nexoramarket.ms_pedidos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String PAGADO = "PAGADO";
    public static final String RECHAZADO = "RECHAZADO";
    public static final String CANCELADO = "CANCELADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** oid (o sub) del usuario en Entra ID: viene del JWT, nunca del body. */
    @Column(nullable = false, length = 64)
    private String clienteId;

    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Lo calcula el backend con los precios del catálogo local. */
    @Column(nullable = false)
    private Double total;

    @Column(nullable = false, length = 20)
    private String estado;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPedido> items = new ArrayList<>();

    public Pedido(Long id, String clienteId, LocalDateTime fecha, Double total, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
    }

    public void agregarItem(ItemPedido item) {
        item.setPedido(this);
        items.add(item);
    }
}
