import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { PRODUCTOS } from '../mock/datos-mock';
import {
  AlertaStock,
  EventoKafka,
  ResumenAnalitica,
  TopProducto,
  VentaPorMinuto,
} from '../models/analitica';

const TOPICOS = [
  'nexora.pedidos.eventos',
  'nexora.inventario.eventos',
  'nexora.pedidos.eventos.DLT',
];

const TIPOS_EVENTO = ['PEDIDO_CREADO', 'PAGO_APROBADO', 'BOLETA_GENERADA', 'STOCK_ACTUALIZADO'];

interface EstadoSimulado {
  pedidos: number;
  ventas: number;
  ingreso: number;
  offset: number;
}

const ESTADO: EstadoSimulado = {
  pedidos: 128,
  ventas: 342,
  ingreso: 12450320,
  offset: 4096,
};

function aleatorio(minimo: number, maximo: number): number {
  return Math.floor(Math.random() * (maximo - minimo + 1)) + minimo;
}

@Injectable({ providedIn: 'root' })
export class AnaliticaService {
  resumen(): Observable<ResumenAnalitica> {
    ESTADO.pedidos += aleatorio(0, 2);
    ESTADO.ventas += aleatorio(0, 4);
    ESTADO.ingreso += aleatorio(0, 180000);

    return of({
      pedidos: ESTADO.pedidos,
      ventas: ESTADO.ventas,
      ingreso: ESTADO.ingreso,
    }).pipe(delay(200));
  }

  ventasPorMinuto(): Observable<VentaPorMinuto[]> {
    const ahora = new Date();
    const puntos: VentaPorMinuto[] = [];

    for (let indice = 11; indice >= 0; indice--) {
      const momento = new Date(ahora.getTime() - indice * 60_000);
      const cantidad = aleatorio(2, 14);
      puntos.push({
        minuto: momento.toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit' }),
        cantidad,
        monto: cantidad * aleatorio(15000, 45000),
      });
    }

    return of(puntos).pipe(delay(250));
  }

  topProductos(): Observable<TopProducto[]> {
    const top = PRODUCTOS.slice(0, 5)
      .map((producto, indice) => {
        const cantidad = aleatorio(8, 40) - indice * 3;
        return {
          productoId: producto.id,
          nombre: producto.nombre,
          cantidad: Math.max(cantidad, 1),
          ingreso: Math.max(cantidad, 1) * producto.precio,
        };
      })
      .sort((a, b) => b.cantidad - a.cantidad);

    return of(top).pipe(delay(300));
  }

  eventosRecientes(): Observable<EventoKafka[]> {
    const eventos: EventoKafka[] = [];

    for (let indice = 0; indice < 8; indice++) {
      ESTADO.offset += aleatorio(1, 3);
      eventos.push({
        topico: TOPICOS[aleatorio(0, TOPICOS.length - 1)],
        particion: aleatorio(0, 2),
        offset: ESTADO.offset - indice,
        tipo: TIPOS_EVENTO[aleatorio(0, TIPOS_EVENTO.length - 1)],
        timestamp: new Date(Date.now() - indice * 7000).toISOString(),
      });
    }

    return of(eventos).pipe(delay(200));
  }

  alertas(): Observable<AlertaStock[]> {
    const alertas = PRODUCTOS.filter((producto) => producto.stock <= 10).map((producto) => ({
      productoId: producto.id,
      nombre: producto.nombre,
      stock: producto.stock,
    }));

    return of(alertas).pipe(delay(250));
  }
}
