import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Boleta } from '../models/boleta';

@Injectable({ providedIn: 'root' })
export class BoletaService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Boleta[]> {
    return this.http.get<Boleta[]>('/api/boletas');
  }

  buscarPorPedido(pedidoId: number): Observable<Boleta> {
    return this.http.get<Boleta>(`/api/boletas/pedido/${pedidoId}`);
  }
}
