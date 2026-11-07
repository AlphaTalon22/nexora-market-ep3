import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { ItemCarrito } from '../models/item-carrito';
import { EstadoPedido, Pedido } from '../models/pedido';

const CLAVE_ALMACENAMIENTO = 'nexora.pedidos';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private pedidos: Pedido[] = this.leer();

  listar(): Observable<Pedido[]> {
    const ordenados = [...this.pedidos].sort((a, b) => b.fecha.localeCompare(a.fecha));
    return of(ordenados).pipe(delay(300));
  }

  obtener(id: number): Observable<Pedido | undefined> {
    return of(this.pedidos.find((pedido) => pedido.id === id)).pipe(delay(200));
  }

  crear(datos: { clienteId: number; total: number; items: ItemCarrito[] }): Observable<Pedido> {
    const pedido: Pedido = {
      id: this.siguienteId(),
      clienteId: datos.clienteId,
      fecha: new Date().toISOString(),
      total: datos.total,
      estado: 'PENDIENTE',
      items: datos.items,
    };

    this.pedidos = [pedido, ...this.pedidos];
    this.guardar();
    return of(pedido).pipe(delay(400));
  }

  cambiarEstado(id: number, estado: EstadoPedido): void {
    this.pedidos = this.pedidos.map((pedido) => (pedido.id === id ? { ...pedido, estado } : pedido));
    this.guardar();
  }

  private siguienteId(): number {
    return this.pedidos.reduce((maximo, pedido) => Math.max(maximo, pedido.id), 0) + 1;
  }

  private leer(): Pedido[] {
    try {
      const crudo = localStorage.getItem(CLAVE_ALMACENAMIENTO);
      if (crudo) {
        return JSON.parse(crudo) as Pedido[];
      }
    } catch {
      localStorage.removeItem(CLAVE_ALMACENAMIENTO);
    }

    const ahora = Date.now();
    return [
      {
        id: 2,
        clienteId: 1,
        fecha: new Date(ahora - 86_400_000).toISOString(),
        total: 74980,
        estado: 'PAGADO',
      },
      {
        id: 1,
        clienteId: 1,
        fecha: new Date(ahora - 172_800_000).toISOString(),
        total: 39990,
        estado: 'PENDIENTE',
      },
    ];
  }

  private guardar(): void {
    localStorage.setItem(CLAVE_ALMACENAMIENTO, JSON.stringify(this.pedidos));
  }
}
