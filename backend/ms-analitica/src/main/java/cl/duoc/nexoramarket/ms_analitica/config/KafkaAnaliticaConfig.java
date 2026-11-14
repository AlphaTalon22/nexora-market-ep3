package cl.duoc.nexoramarket.ms_analitica.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.ms_analitica.exception.EventoInvalidoException;
import jakarta.validation.ConstraintViolationException;

/**
 * Configuración de los dos consumidores de analítica.
 * <p>
 * Cada container factory parte de {@link KafkaProperties#buildConsumerProperties()}
 * (que ya trae {@code ErrorHandlingDeserializer} con
 * {@code JacksonJsonDeserializer} como delegado) y solo sobrescribe el tipo por
 * defecto del valor. Así un JSON malformado falla en el deserializador y termina
 * en el DLT en lugar de bloquear la partición.
 */
@Configuration
@EnableKafka
public class KafkaAnaliticaConfig implements KafkaListenerConfigurer {

	private static final String PAQUETES_CONFIABLES = "cl.duoc.nexoramarket.common.kafka";

	private final KafkaProperties kafkaProperties;

	public KafkaAnaliticaConfig(KafkaProperties kafkaProperties) {
		this.kafkaProperties = kafkaProperties;
	}

	@Bean
	public LocalValidatorFactoryBean validadorEventosListener() {
		return new LocalValidatorFactoryBean();
	}

	@Override
	public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
		registrar.setValidator(validadorEventosListener());
	}

	/**
	 * Backoff exponencial 1s/2s/4s (3 reintentos) y DLT con dos plantillas:
	 * {@code byte[]} para los registros cuya deserialización falló y {@code Object}
	 * (JSON Jackson 3) para el resto.
	 */
	@Bean
	public DefaultErrorHandler errorHandlerKafka() {
		ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
		backOff.setInitialInterval(1000L);
		backOff.setMultiplier(2.0);

		Map<Class<?>, KafkaOperations<?, ?>> plantillas = new LinkedHashMap<>();
		plantillas.put(byte[].class, new KafkaTemplate<>(
				new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(),
						new ByteArraySerializer(), new ByteArraySerializer())));
		plantillas.put(Object.class, new KafkaTemplate<>(
				new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(),
						new StringSerializer(), new JacksonJsonSerializer<>())));

		DefaultErrorHandler handler = new DefaultErrorHandler(
				new DeadLetterPublishingRecoverer(plantillas), backOff);
		handler.addNotRetryableExceptions(DeserializationException.class,
				MethodArgumentNotValidException.class, ConstraintViolationException.class,
				EventoInvalidoException.class);
		return handler;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PedidoEvento> pedidoListenerContainerFactory() {
		return crearContainerFactory(PedidoEvento.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, InventarioEvento> inventarioListenerContainerFactory() {
		return crearContainerFactory(InventarioEvento.class);
	}

	private <T> ConcurrentKafkaListenerContainerFactory<String, T> crearContainerFactory(Class<T> tipo) {
		Map<String, Object> propiedades = new HashMap<>(kafkaProperties.buildConsumerProperties());
		propiedades.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, tipo.getName());
		propiedades.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, PAQUETES_CONFIABLES);
		propiedades.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

		ConcurrentKafkaListenerContainerFactory<String, T> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(propiedades));
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
		factory.setCommonErrorHandler(errorHandlerKafka());
		return factory;
	}
}
