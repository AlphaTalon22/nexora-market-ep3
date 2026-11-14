package cl.duoc.nexoramarket.ms_analitica.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import cl.duoc.nexoramarket.common.kafka.InventarioEvento;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import cl.duoc.nexoramarket.ms_analitica.service.InventarioAnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventarioEventListener {

	private final InventarioAnalyticsService inventarioAnalyticsService;

	@KafkaListener(topics = Topicos.INVENTARIO, groupId = "analitica-inventario",
			containerFactory = "inventarioListenerContainerFactory", concurrency = "3")
	public void escuchar(@Valid InventarioEvento evento,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int particion,
			@Header(KafkaHeaders.OFFSET) long offset) {
		inventarioAnalyticsService.procesar(evento, particion, offset);
	}
}
