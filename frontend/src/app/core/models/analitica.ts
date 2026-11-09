export interface ResumenAnalitica {
  pedidos: number;
  ventas: number;
  ingreso: number;
}

export interface VentaPorMinuto {
  minuto: string;
  cantidad: number;
  monto: number;
}

export interface TopProducto {
  productoId: number;
  nombre: string;
  cantidad: number;
  ingreso: number;
}

export interface EventoKafka {
  topico: string;
  particion: number;
  offset: number;
  tipo: string;
  timestamp: string;
}

export interface AlertaStock {
  productoId: number;
  nombre: string;
  stock: number;
}
