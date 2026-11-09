import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { CLIENTE_MOCK, buscarProducto } from '../mock/datos-mock';
import { ItemCarrito } from '../models/item-carrito';
import { CrearPedidoRequest, EstadoPedido, Pedido } from '../models/pedido';

const CLAVE_ALMACENAMIENTO = 'nexora.pedidos';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private pedidos: Pedido[] = this.leer();

  listar(): Observable<Pedido[]> {
    const ordenados = [...this.pedidos].sort((a, b) => b.fecha.localeCompare(a.fecha));
    return of(ordenados).pipe(delay(300));
  }

  obtener(id: number): Observable<Pedido | undefined> {
    return of(this.buscar(id)).pipe(delay(200));
  }

  buscar(id: number): Pedido | undefined {
    return this.pedidos.find((pedido) => pedido.id === id);
  }

  crear(datos: CrearPedidoRequest): Observable<Pedido> {
    const items: ItemCarrito[] = datos.items.map((linea) => {
      const producto = buscarProducto(linea.productoId);
      return {
        productoId: linea.productoId,
        nombreProducto: producto?.nombre ?? `Producto ${linea.productoId}`,
        precioUnitario: producto?.precio ?? 0,
        cantidad: linea.cantidad,
        imagen: producto?.imagen,
      };
    });

    const total = items.reduce((suma, item) => suma + item.precioUnitario * item.cantidad, 0);

    const pedido: Pedido = {
      id: this.siguienteId(),
      clienteId: CLIENTE_MOCK,
      fecha: new Date().toISOString(),
      total,
      estado: 'PENDIENTE',
      items,
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
        clienteId: CLIENTE_MOCK,
        fecha: new Date(ahora - 86_400_000).toISOString(),
        total: 74980,
        estado: 'PAGADO',
      },
      {
        id: 1,
        clienteId: CLIENTE_MOCK,
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
