import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { Notificacion } from '../models/notificacion';

const NOTIFICACIONES: Notificacion[] = [
  {
    id: 3,
    mensaje: 'Tu pago del pedido #2 fue APROBADO. La boleta fue generada.',
    fecha: new Date(Date.now() - 86_000_000).toISOString(),
    tipo: 'PAGO',
  },
  {
    id: 2,
    mensaje: 'Recibimos tu pedido #2 por $74.980. Estado: PENDIENTE.',
    fecha: new Date(Date.now() - 86_300_000).toISOString(),
    tipo: 'PEDIDO',
  },
  {
    id: 1,
    mensaje: 'Boleta electrónica disponible para el pago #1.',
    fecha: new Date(Date.now() - 172_000_000).toISOString(),
    tipo: 'BOLETA',
  },
];

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  listar(): Observable<Notificacion[]> {
    return of(NOTIFICACIONES).pipe(delay(300));
  }
}
