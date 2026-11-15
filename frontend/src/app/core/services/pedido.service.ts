import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CrearPedidoRequest, Pedido } from '../models/pedido';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>('/api/pedidos');
  }

  obtener(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`/api/pedidos/${id}`);
  }

  crear(datos: CrearPedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>('/api/pedidos', datos);
  }

  cancelar(id: number): Observable<Pedido> {
    return this.http.post<Pedido>(`/api/pedidos/${id}/cancelar`, null);
  }
}
