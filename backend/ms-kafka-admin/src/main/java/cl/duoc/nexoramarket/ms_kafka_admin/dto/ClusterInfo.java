package cl.duoc.nexoramarket.ms_kafka_admin.dto;

import java.util.List;

public record ClusterInfo(
		List<BrokerInfo> brokers,
		BrokerInfo controller) {
}
