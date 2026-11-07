export interface Notificacion {
  id: number;
  mensaje: string;
  fecha: string;
  tipo: 'PEDIDO' | 'PAGO' | 'BOLETA';
}
