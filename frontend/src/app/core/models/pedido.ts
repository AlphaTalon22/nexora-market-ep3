import { ItemCarrito } from './item-carrito';

export type EstadoPedido = 'PENDIENTE' | 'PAGADO' | 'RECHAZADO' | 'CANCELADO';

export interface ItemPedidoRequest {
  productoId: number;
  cantidad: number;
}

export interface CrearPedidoRequest {
  items: ItemPedidoRequest[];
}

export interface Pedido {
  id: number;
  clienteId: string;
  fecha: string;
  total: number;
  estado: EstadoPedido;
  items?: ItemCarrito[];
}
