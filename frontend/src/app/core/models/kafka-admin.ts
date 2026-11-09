export interface TopicoKafka {
  nombre: string;
  particiones: number;
  replicas: number;
  isr: number;
  retencionMs: number;
}

export interface ConsumerGroupKafka {
  grupo: string;
  topico: string;
  lag: number;
  estado: string;
}
