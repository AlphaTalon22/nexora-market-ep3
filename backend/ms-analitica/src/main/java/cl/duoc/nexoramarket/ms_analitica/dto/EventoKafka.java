package cl.duoc.nexoramarket.ms_analitica.dto;

import java.time.LocalDateTime;

public record EventoKafka(String topico, int particion, long offset, String tipo, LocalDateTime timestamp) {
}
