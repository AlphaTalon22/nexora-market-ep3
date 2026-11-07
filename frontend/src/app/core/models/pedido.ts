import { ItemCarrito } from './item-carrito';

export type EstadoPedido = 'PENDIENTE' | 'PAGADO' | 'RECHAZADO' | 'CANCELADO';

export interface Pedido {
  id: number;
  clienteId: number;
  fecha: string;
  total: number;
  estado: EstadoPedido;
  items?: ItemCarrito[];
}
