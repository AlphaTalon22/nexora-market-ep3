export interface CrearPagoRequest {
  pedidoId: number;
  metodoPago: string;
  ultimos4: string;
}

export interface Pago {
  id: number;
  pedidoId: number;
  monto: number;
  metodoPago: string;
  ultimos4?: string;
  estado: string;
  fechaPago: string;
}
