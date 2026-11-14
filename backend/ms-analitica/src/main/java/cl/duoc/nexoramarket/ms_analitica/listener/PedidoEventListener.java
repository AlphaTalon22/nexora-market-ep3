package cl.duoc.nexoramarket.ms_analitica.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import cl.duoc.nexoramarket.common.kafka.PedidoEvento;
import cl.duoc.nexoramarket.common.kafka.Topicos;
import cl.duoc.nexoramarket.ms_analitica.service.PedidoAnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PedidoEventListener {

	private final PedidoAnalyticsService pedidoAnalyticsService;

	@KafkaListener(topics = Topicos.PEDIDOS, groupId = "analitica-pedidos",
			containerFactory = "pedidoListenerContainerFactory", concurrency = "3")
	public void escuchar(@Valid PedidoEvento evento,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int particion,
			@Header(KafkaHeaders.OFFSET) long offset) {
		pedidoAnalyticsService.procesar(evento, particion, offset);
	}
}
