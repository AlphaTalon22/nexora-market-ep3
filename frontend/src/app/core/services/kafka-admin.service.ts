import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { ConsumerGroupKafka, TopicoKafka } from '../models/kafka-admin';

const TOPICOS: TopicoKafka[] = [
  { nombre: 'nexora.pedidos.eventos', particiones: 3, replicas: 3, isr: 3, retencionMs: 604800000 },
  { nombre: 'nexora.inventario.eventos', particiones: 3, replicas: 3, isr: 3, retencionMs: 2592000000 },
  { nombre: 'nexora.pedidos.eventos.DLT', particiones: 3, replicas: 3, isr: 3, retencionMs: 1209600000 },
  { nombre: 'nexora.inventario.eventos.DLT', particiones: 3, replicas: 3, isr: 3, retencionMs: 1209600000 },
];

const CONSUMER_GROUPS: ConsumerGroupKafka[] = [
  { grupo: 'ms-analitica', topico: 'nexora.pedidos.eventos', lag: 0, estado: 'Stable' },
  { grupo: 'ms-analitica', topico: 'nexora.inventario.eventos', lag: 2, estado: 'Stable' },
  { grupo: 'ms-pedidos', topico: 'nexora.pedidos.eventos', lag: 0, estado: 'Stable' },
];

@Injectable({ providedIn: 'root' })
export class KafkaAdminService {
  listarTopicos(): Observable<TopicoKafka[]> {
    return of([...TOPICOS]).pipe(delay(300));
  }

  detalleTopico(nombre: string): Observable<TopicoKafka | undefined> {
    return of(TOPICOS.find((topico) => topico.nombre === nombre)).pipe(delay(200));
  }

  consumerGroups(): Observable<ConsumerGroupKafka[]> {
    return of([...CONSUMER_GROUPS]).pipe(delay(250));
  }
}
