import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { BindingRabbit, ColaRabbit, ExchangeRabbit, NodoRabbit } from '../models/rabbit-admin';

const NODOS: NodoRabbit[] = [
  { nombre: 'rabbit@rabbitmq-1', estado: 'running' },
  { nombre: 'rabbit@rabbitmq-2', estado: 'running' },
];

const COLAS: ColaRabbit[] = [
  { nombre: 'nexora.notificaciones.q', mensajes: 0, consumidores: 1, dlq: 'nexora.notificaciones.dlq' },
  { nombre: 'nexora.pedidos.pagos.q', mensajes: 0, consumidores: 1, dlq: 'nexora.pedidos.pagos.dlq' },
  { nombre: 'nexora.boletas.q', mensajes: 0, consumidores: 1, dlq: 'nexora.boletas.dlq' },
];

const EXCHANGES: ExchangeRabbit[] = [
  { nombre: 'nexora.eventos.topic', tipo: 'topic' },
  { nombre: 'nexora.documentos.direct', tipo: 'direct' },
  { nombre: 'nexora.dlx.direct', tipo: 'direct' },
];

const BINDINGS: BindingRabbit[] = [
  { exchange: 'nexora.eventos.topic', cola: 'nexora.notificaciones.q', routingKey: 'pedido.*' },
  { exchange: 'nexora.eventos.topic', cola: 'nexora.notificaciones.q', routingKey: 'pago.*' },
  { exchange: 'nexora.eventos.topic', cola: 'nexora.pedidos.pagos.q', routingKey: 'pago.*' },
  { exchange: 'nexora.documentos.direct', cola: 'nexora.boletas.q', routingKey: 'boleta.generar' },
];

@Injectable({ providedIn: 'root' })
export class RabbitAdminService {
  nodos(): Observable<NodoRabbit[]> {
    return of([...NODOS]).pipe(delay(200));
  }

  colas(): Observable<ColaRabbit[]> {
    return of([...COLAS]).pipe(delay(250));
  }

  exchanges(): Observable<ExchangeRabbit[]> {
    return of([...EXCHANGES]).pipe(delay(250));
  }

  bindings(): Observable<BindingRabbit[]> {
    return of([...BINDINGS]).pipe(delay(250));
  }
}
