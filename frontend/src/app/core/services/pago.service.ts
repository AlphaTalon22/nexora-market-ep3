import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CrearPagoRequest, Pago } from '../models/pago';

@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Pago[]> {
    return this.http.get<Pago[]>('/api/pagos');
  }

  procesar(datos: CrearPagoRequest): Observable<Pago> {
    return this.http.post<Pago>('/api/pagos', datos);
  }

  buscarPorPedido(pedidoId: number): Observable<Pago> {
    return this.http.get<Pago>(`/api/pagos/pedido/${pedidoId}`);
  }
}
