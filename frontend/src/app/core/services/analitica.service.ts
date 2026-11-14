import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AlertaStock,
  EventoKafka,
  ResumenAnalitica,
  TopProducto,
  VentaPorMinuto,
} from '../models/analitica';

@Injectable({ providedIn: 'root' })
export class AnaliticaService {
  private readonly http = inject(HttpClient);

  resumen(): Observable<ResumenAnalitica> {
    return this.http.get<ResumenAnalitica>('/api/analitica/resumen');
  }

  ventasPorMinuto(): Observable<VentaPorMinuto[]> {
    return this.http.get<VentaPorMinuto[]>('/api/analitica/ventas-por-minuto');
  }

  topProductos(): Observable<TopProducto[]> {
    return this.http.get<TopProducto[]>('/api/analitica/top-productos');
  }

  eventosRecientes(): Observable<EventoKafka[]> {
    return this.http.get<EventoKafka[]>('/api/analitica/eventos-recientes');
  }

  alertas(): Observable<AlertaStock[]> {
    return this.http.get<AlertaStock[]>('/api/analitica/alertas');
  }
}
