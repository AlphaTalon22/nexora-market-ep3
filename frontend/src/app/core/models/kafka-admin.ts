export interface BrokerInfo {
  id: number;
  host: string;
  puerto: number;
}

export interface ClusterInfo {
  brokers: BrokerInfo[];
  controller: BrokerInfo;
}

export interface TopicoKafka {
  nombre: string;
  particiones: number;
  replicas: number;
  isr: number;
  retencionMs: number;
}

export interface ConsumerGroupKafka {
  grupo: string;
  estado: string;
  lagTotal: number;
}
