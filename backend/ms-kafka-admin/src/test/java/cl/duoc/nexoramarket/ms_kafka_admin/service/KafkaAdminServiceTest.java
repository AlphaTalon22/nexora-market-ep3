package cl.duoc.nexoramarket.ms_kafka_admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.duoc.nexoramarket.ms_kafka_admin.dto.BrokerInfo;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.ClusterInfo;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.CrearTopicoRequest;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.GrupoDetalle;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.TopicoDetalle;
import cl.duoc.nexoramarket.ms_kafka_admin.dto.TopicoResumen;
import cl.duoc.nexoramarket.ms_kafka_admin.exception.RecursoNoEncontradoException;
import cl.duoc.nexoramarket.ms_kafka_admin.exception.RecursoYaExisteException;
import cl.duoc.nexoramarket.ms_kafka_admin.exception.SolicitudInvalidaException;
import cl.duoc.nexoramarket.ms_kafka_admin.exception.TopicoProtegidoException;

class KafkaAdminServiceTest {

	private static final Node B1 = new Node(1, "kafka1", 9092);
	private static final Node B2 = new Node(2, "kafka2", 9093);
	private static final Node B3 = new Node(3, "kafka3", 9094);

	private AdminClient admin;
	private KafkaAdminService service;

	@BeforeEach
	void setUp() {
		admin = mock(AdminClient.class);
		service = new KafkaAdminService(admin);
	}

	private void clusterConTresBrokers() {
		DescribeClusterResult resultado = mock(DescribeClusterResult.class);
		when(resultado.nodes()).thenReturn(KafkaFuture.completedFuture(List.of(B1, B2, B3)));
		when(resultado.controller()).thenReturn(KafkaFuture.completedFuture(B1));
		when(admin.describeCluster()).thenReturn(resultado);
	}

	private DescribeTopicsResult descripcionDe(String nombre, TopicPartitionInfo... particiones) {
		TopicDescription descripcion = new TopicDescription(nombre, false, List.of(particiones));
		DescribeTopicsResult resultado = mock(DescribeTopicsResult.class);
		when(resultado.allTopicNames()).thenReturn(KafkaFuture.completedFuture(Map.of(nombre, descripcion)));
		when(admin.describeTopics(anyCollection())).thenReturn(resultado);
		return resultado;
	}

	private DescribeConfigsResult configuracionDe(String nombre, String clave, String valor) {
		ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
		Config config = new Config(List.of(new ConfigEntry(clave, valor)));
		DescribeConfigsResult resultado = mock(DescribeConfigsResult.class);
		when(resultado.all()).thenReturn(KafkaFuture.completedFuture(Map.of(recurso, config)));
		when(admin.describeConfigs(anyCollection())).thenReturn(resultado);
		return resultado;
	}

	private ListTopicsResult topicosExistentes(String... nombres) {
		ListTopicsResult resultado = mock(ListTopicsResult.class);
		when(resultado.names()).thenReturn(KafkaFuture.completedFuture(Set.of(nombres)));
		when(admin.listTopics()).thenReturn(resultado);
		return resultado;
	}

	// ============================ Cluster ============================

	@Test
	void clusterDevuelveBrokersOrdenadosYController() {
		DescribeClusterResult resultado = mock(DescribeClusterResult.class);
		when(resultado.nodes()).thenReturn(KafkaFuture.completedFuture(List.of(B3, B1, B2)));
		when(resultado.controller()).thenReturn(KafkaFuture.completedFuture(B2));
		when(admin.describeCluster()).thenReturn(resultado);

		ClusterInfo info = service.cluster();

		assertThat(info.brokers()).extracting(BrokerInfo::id).containsExactly(1, 2, 3);
		assertThat(info.brokers()).extracting(BrokerInfo::host).containsExactly("kafka1", "kafka2", "kafka3");
		assertThat(info.controller().id()).isEqualTo(2);
	}

	// ============================ Tópicos ============================

	@Test
	void listarTopicosIncluyeParticionesReplicasIsrYRetencion() {
		topicosExistentes("nexora.pedidos.eventos");
		descripcionDe("nexora.pedidos.eventos",
				new TopicPartitionInfo(0, B1, List.of(B1, B2, B3), List.of(B1, B2, B3)));
		configuracionDe("nexora.pedidos.eventos", "retention.ms", "604800000");

		List<TopicoResumen> topicos = service.listarTopicos();

		assertThat(topicos).singleElement().satisfies(topico -> {
			assertThat(topico.nombre()).isEqualTo("nexora.pedidos.eventos");
			assertThat(topico.particiones()).isEqualTo(1);
			assertThat(topico.replicas()).isEqualTo(3);
			assertThat(topico.isr()).isEqualTo(3);
			assertThat(topico.retencionMs()).isEqualTo(604800000L);
		});
	}

	@Test
	void detalleTopicoCalculaOffsetsPorParticion() {
		String nombre = "nexora.inventario.eventos";
		descripcionDe(nombre, new TopicPartitionInfo(0, B1, List.of(B1, B2), List.of(B1, B2)));
		configuracionDe(nombre, "retention.ms", "86400000");

		TopicPartition tp = new TopicPartition(nombre, 0);
		ListOffsetsResult inicios = mock(ListOffsetsResult.class);
		when(inicios.all()).thenReturn(KafkaFuture.completedFuture(
				Map.of(tp, new ListOffsetsResultInfo(0L, -1L, java.util.Optional.empty()))));
		ListOffsetsResult fines = mock(ListOffsetsResult.class);
		when(fines.all()).thenReturn(KafkaFuture.completedFuture(
				Map.of(tp, new ListOffsetsResultInfo(120L, -1L, java.util.Optional.empty()))));
		when(admin.listOffsets(anyMap())).thenReturn(inicios, fines);

		TopicoDetalle detalle = service.detalleTopico(nombre);

		assertThat(detalle.particiones()).singleElement().satisfies(particion -> {
			assertThat(particion.particion()).isZero();
			assertThat(particion.lider()).isEqualTo(1);
			assertThat(particion.replicas()).containsExactly(1, 2);
			assertThat(particion.offsetInicio()).isZero();
			assertThat(particion.offsetFin()).isEqualTo(120L);
		});
		assertThat(detalle.configuracion()).containsEntry("retention.ms", "86400000");
	}

	@Test
	void topicoInexistenteResponde404() throws Exception {
		@SuppressWarnings("unchecked")
		KafkaFuture<Map<String, TopicDescription>> fallo = mock(KafkaFuture.class);
		when(fallo.get(anyLong(), any(TimeUnit.class))).thenThrow(
				new ExecutionException(new UnknownTopicOrPartitionException("Topic no.existe not found")));
		DescribeTopicsResult resultado = mock(DescribeTopicsResult.class);
		when(resultado.allTopicNames()).thenReturn(fallo);
		when(admin.describeTopics(anyCollection())).thenReturn(resultado);

		assertThatThrownBy(() -> service.detalleTopico("no.existe"))
				.isInstanceOf(RecursoNoEncontradoException.class)
				.hasMessageContaining("no.existe");
	}

	@Test
	void crearTopicoRechazaMasReplicasQueBrokers() {
		clusterConTresBrokers();

		assertThatThrownBy(() -> service.crearTopico(
				new CrearTopicoRequest("nexora.demo.eventos", 2, 5, null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("3 brokers");
		verify(admin, never()).createTopics(anyCollection());
	}

	@Test
	void crearTopicoExistenteResponde409() {
		clusterConTresBrokers();
		topicosExistentes("nexora.demo.eventos");

		assertThatThrownBy(() -> service.crearTopico(
				new CrearTopicoRequest("nexora.demo.eventos", 2, 2, null)))
				.isInstanceOf(RecursoYaExisteException.class);
		verify(admin, never()).createTopics(anyCollection());
	}

	@Test
	void crearTopicoValidoLlamaAlAdmin() {
		clusterConTresBrokers();
		topicosExistentes("otro.topico");
		CreateTopicsResult resultado = mock(CreateTopicsResult.class);
		when(resultado.all()).thenReturn(KafkaFuture.completedFuture(null));
		when(admin.createTopics(anyCollection())).thenReturn(resultado);

		service.crearTopico(new CrearTopicoRequest("nexora.demo.eventos", 2, 3,
				Map.of("retention.ms", "86400000")));

		verify(admin).createTopics(anyCollection());
	}

	@Test
	void eliminarTopicoDelNegocioSinForzarResponde403() {
		assertThatThrownBy(() -> service.eliminarTopico("nexora.pedidos.eventos", false))
				.isInstanceOf(TopicoProtegidoException.class)
				.hasMessageContaining("forzar=true");
		verify(admin, never()).deleteTopics(anyCollection());
	}

	@Test
	void aumentarParticionesSoloPermiteSubir() {
		descripcionDe("nexora.demo.eventos",
				new TopicPartitionInfo(0, B1, List.of(B1), List.of(B1)),
				new TopicPartitionInfo(1, B2, List.of(B2), List.of(B2)));

		assertThatThrownBy(() -> service.aumentarParticiones("nexora.demo.eventos", 2))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("solo se pueden aumentar");
	}

	// ======================= Consumer groups ========================

	@Test
	void listarGruposSinGruposDevuelveListaVacia() {
		ListConsumerGroupsResult resultado = mock(ListConsumerGroupsResult.class);
		when(resultado.all()).thenReturn(KafkaFuture.completedFuture(List.of()));
		when(admin.listConsumerGroups()).thenReturn(resultado);

		assertThat(service.listarGrupos()).isEmpty();
	}

	@Test
	void detalleGrupoCalculaLagFinalMenosConsumido() {
		String grupo = "productos-stock";
		String topico = "nexora.inventario.eventos";
		TopicPartition tp = new TopicPartition(topico, 0);

		ConsumerGroupDescription descripcion = mock(ConsumerGroupDescription.class);
		when(descripcion.state()).thenReturn(ConsumerGroupState.STABLE);
		when(descripcion.members()).thenReturn(List.of());
		DescribeConsumerGroupsResult grupos = mock(DescribeConsumerGroupsResult.class);
		when(grupos.all()).thenReturn(KafkaFuture.completedFuture(Map.of(grupo, descripcion)));
		when(admin.describeConsumerGroups(List.of(grupo))).thenReturn(grupos);

		ListConsumerGroupOffsetsResult offsets = mock(ListConsumerGroupOffsetsResult.class);
		when(offsets.partitionsToOffsetAndMetadata())
				.thenReturn(KafkaFuture.completedFuture(Map.of(tp, new OffsetAndMetadata(90L))));
		when(admin.listConsumerGroupOffsets(grupo)).thenReturn(offsets);

		ListOffsetsResult fines = mock(ListOffsetsResult.class);
		when(fines.all()).thenReturn(KafkaFuture.completedFuture(
				Map.of(tp, new ListOffsetsResultInfo(100L, -1L, java.util.Optional.empty()))));
		when(admin.listOffsets(anyMap())).thenReturn(fines);

		GrupoDetalle detalle = service.detalleGrupo(grupo);

		assertThat(detalle.grupo()).isEqualTo(grupo);
		assertThat(detalle.estado()).isEqualTo("STABLE");
		assertThat(detalle.miembros()).isEmpty();
		assertThat(detalle.lagPorParticion()).singleElement().satisfies(lag -> {
			assertThat(lag.topico()).isEqualTo(topico);
			assertThat(lag.particion()).isZero();
			assertThat(lag.offsetConsumido()).isEqualTo(90L);
			assertThat(lag.offsetFinal()).isEqualTo(100L);
			assertThat(lag.lag()).isEqualTo(10L);
		});
	}
}
