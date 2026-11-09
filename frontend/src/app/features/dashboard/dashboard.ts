import { DatePipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { forkJoin } from 'rxjs';
import {
  AlertaStock,
  EventoKafka,
  ResumenAnalitica,
  TopProducto,
  VentaPorMinuto,
} from '../../core/models/analitica';
import { AnaliticaService } from '../../core/services/analitica.service';
import { PrecioClpPipe } from '../../shared/pipes/precio-clp.pipe';

@Component({
  selector: 'app-dashboard',
  imports: [DatePipe, MatIcon, BaseChartDirective, PrecioClpPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly analitica = inject(AnaliticaService);
  private readonly destroyRef = inject(DestroyRef);

  readonly cargando = signal(true);
  readonly actualizado = signal<Date | null>(null);
  readonly resumen = signal<ResumenAnalitica>({ pedidos: 0, ventas: 0, ingreso: 0 });
  readonly ventas = signal<VentaPorMinuto[]>([]);
  readonly topProductos = signal<TopProducto[]>([]);
  readonly eventos = signal<EventoKafka[]>([]);
  readonly alertas = signal<AlertaStock[]>([]);

  readonly lineChartData = computed<ChartData<'line'>>(() => ({
    labels: this.ventas().map((punto) => punto.minuto),
    datasets: [
      {
        data: this.ventas().map((punto) => punto.cantidad),
        label: 'Ventas por minuto',
        borderColor: '#1d4ed8',
        backgroundColor: 'rgba(29, 78, 216, 0.14)',
        pointBackgroundColor: '#1d4ed8',
        fill: true,
        tension: 0.35,
        pointRadius: 3,
      },
    ],
  }));

  readonly lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
    },
    scales: {
      y: { beginAtZero: true, ticks: { precision: 0 } },
    },
  };

  readonly maxTopProducto = computed(() =>
    this.topProductos().reduce((maximo, producto) => Math.max(maximo, producto.cantidad), 1),
  );

  private intervalo?: ReturnType<typeof setInterval>;

  constructor() {
    this.cargar();
    this.intervalo = setInterval(() => this.cargar(), 2000);
    this.destroyRef.onDestroy(() => clearInterval(this.intervalo));
  }

  porcentaje(producto: TopProducto): number {
    return Math.round((producto.cantidad / this.maxTopProducto()) * 100);
  }

  private cargar(): void {
    forkJoin({
      resumen: this.analitica.resumen(),
      ventas: this.analitica.ventasPorMinuto(),
      topProductos: this.analitica.topProductos(),
      eventos: this.analitica.eventosRecientes(),
      alertas: this.analitica.alertas(),
    }).subscribe((datos) => {
      this.resumen.set(datos.resumen);
      this.ventas.set(datos.ventas);
      this.topProductos.set(datos.topProductos);
      this.eventos.set(datos.eventos);
      this.alertas.set(datos.alertas);
      this.actualizado.set(new Date());
      this.cargando.set(false);
    });
  }
}
