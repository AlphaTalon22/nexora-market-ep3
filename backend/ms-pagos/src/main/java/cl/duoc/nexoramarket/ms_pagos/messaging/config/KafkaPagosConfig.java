package cl.duoc.nexoramarket.ms_pagos.messaging.config;

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

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import jakarta.validation.ConstraintViolationException;

/**
 * Consumidor pagos-pedidos: lee {@code nexora.pedidos.eventos} y mantiene la copia
 * local de los pedidos por pagar.
 * <p>
 * El value deserializer es {@code ErrorHandlingDeserializer} con
 * {@code JacksonJsonDeserializer} como delegado: un JSON malformado termina en el
 * DLT ({@code nexora.pedidos.eventos.DLT}) y no bloquea la partición.
 */
@Configuration
@EnableKafka
public class KafkaPagosConfig implements KafkaListenerConfigurer {

	private static final String PAQUETES_CONFIABLES = "cl.duoc.nexoramarket.common.kafka";

	private final KafkaProperties kafkaProperties;

	public KafkaPagosConfig(KafkaProperties kafkaProperties) {
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
						new StringSerializer(), new ByteArraySerializer())));
		plantillas.put(Object.class, new KafkaTemplate<>(
				new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(),
						new StringSerializer(), new JacksonJsonSerializer<>())));

		DefaultErrorHandler handler = new DefaultErrorHandler(
				new DeadLetterPublishingRecoverer(plantillas), backOff);
		handler.addNotRetryableExceptions(DeserializationException.class,
				MethodArgumentNotValidException.class, ConstraintViolationException.class);
		return handler;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PedidoEvento> pagosListenerContainerFactory() {
		Map<String, Object> propiedades = new HashMap<>(kafkaProperties.buildConsumerProperties());
		propiedades.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PedidoEvento.class.getName());
		propiedades.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, PAQUETES_CONFIABLES);
		propiedades.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

		ConcurrentKafkaListenerContainerFactory<String, PedidoEvento> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(propiedades));
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
		factory.setCommonErrorHandler(errorHandlerKafka());
		return factory;
	}
}
