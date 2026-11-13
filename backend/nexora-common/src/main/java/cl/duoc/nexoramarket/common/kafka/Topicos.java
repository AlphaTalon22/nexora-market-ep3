package cl.duoc.nexoramarket.common.kafka;

/**
 * Nombres de los tópicos de Kafka del negocio.
 * <p>
 * Los tópicos y sus DLT ya existen en el clúster (los crea {@code kafka-init} en
 * {@code infra/kafka}); estas constantes solo evitan errores de tipeo.
 */
public final class Topicos {

	public static final String PEDIDOS = "nexora.pedidos.eventos";

	public static final String INVENTARIO = "nexora.inventario.eventos";

	public static final String PEDIDOS_DLT = "nexora.pedidos.eventos.DLT";

	public static final String INVENTARIO_DLT = "nexora.inventario.eventos.DLT";

	private Topicos() {
	}
}
