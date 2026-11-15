import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatOption, MatSelect } from '@angular/material/select';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Pedido } from '../../core/models/pedido';
import { PagoService } from '../../core/services/pago.service';
import { PedidoService } from '../../core/services/pedido.service';
import { ToastService } from '../../core/services/toast.service';
import { EstadoBadge } from '../../shared/estado-badge/estado-badge';
import { PrecioClpPipe } from '../../shared/pipes/precio-clp.pipe';

@Component({
  selector: 'app-detalle-pedido',
  imports: [
    RouterLink,
    DatePipe,
    MatButton,
    MatFormField,
    MatLabel,
    MatIcon,
    MatInput,
    MatSelect,
    MatOption,
    EstadoBadge,
    PrecioClpPipe,
  ],
  templateUrl: './detalle-pedido.html',
  styleUrl: './detalle-pedido.css',
})
export class DetallePedido {
  private readonly route = inject(ActivatedRoute);
  private readonly pedidos = inject(PedidoService);
  private readonly pagos = inject(PagoService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly cargando = signal(true);
  readonly pedido = signal<Pedido | null>(null);
  readonly pagando = signal(false);
  readonly metodoPago = signal('TARJETA_CREDITO');
  readonly ultimos4 = signal('1234');

  readonly metodosPago = [
    { valor: 'TARJETA_CREDITO', nombre: 'Tarjeta de crédito' },
    { valor: 'TARJETA_DEBITO', nombre: 'Tarjeta de débito' },
    { valor: 'TRANSFERENCIA', nombre: 'Transferencia bancaria' },
  ];

  private intervalo?: ReturnType<typeof setInterval>;
  private readonly INTERVALO_POLLING_MS = 2500;
  private readonly LIMITE_POLLING_MS = 120_000;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (Number.isNaN(id)) {
      this.cargando.set(false);
      return;
    }

    this.pedidos
      .obtener(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pedido) => {
          this.pedido.set(pedido);
          this.cargando.set(false);
          if (pedido.estado === 'PENDIENTE') {
            this.iniciarPolling(pedido.id);
          }
        },
        error: (error: HttpErrorResponse) => {
          this.cargando.set(false);
          this.toast.error(
            error.status === 404 ? 'Pedido no encontrado.' : 'No se pudo cargar el pedido.',
          );
        },
      });

    this.destroyRef.onDestroy(() => this.detenerPolling());
  }

  pagar(): void {
    const pedido = this.pedido();
    if (!pedido || this.pagando()) {
      return;
    }

    this.pagando.set(true);
    this.pagos
      .procesar({
        pedidoId: pedido.id,
        metodoPago: this.metodoPago(),
        ultimos4: this.ultimos4(),
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.exito('Pago aprobado. Esperando la confirmación del pedido...');
          this.iniciarPolling(pedido.id);
        },
        error: (error: HttpErrorResponse) => {
          this.pagando.set(false);
          this.toast.error(error.error?.error ?? 'No se pudo procesar el pago.');
        },
      });
  }

  private iniciarPolling(id: number): void {
    this.detenerPolling();
    const limite = Date.now() + this.LIMITE_POLLING_MS;

    this.intervalo = setInterval(() => {
      if (Date.now() >= limite) {
        this.detenerPolling();
        this.pagando.set(false);
        this.toast.error(
          'El pago no se confirmó a tiempo. Actualiza la página para ver el estado.',
        );
        return;
      }

      this.pedidos
        .obtener(id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (pedido) => {
            this.pedido.set(pedido);
            if (pedido.estado === 'PENDIENTE') {
              return;
            }

            this.detenerPolling();
            this.pagando.set(false);

            if (pedido.estado === 'PAGADO') {
              this.toast.exito('Pedido confirmado como PAGADO');
            } else if (pedido.estado === 'RECHAZADO') {
              this.toast.error('El pago fue rechazado.');
            } else if (pedido.estado === 'CANCELADO') {
              this.toast.error('El pedido fue cancelado.');
            }
          },
          error: () => {
            this.detenerPolling();
            this.pagando.set(false);
            this.toast.error('No se pudo consultar el estado del pedido.');
          },
        });
    }, this.INTERVALO_POLLING_MS);
  }

  private detenerPolling(): void {
    if (this.intervalo) {
      clearInterval(this.intervalo);
      this.intervalo = undefined;
    }
  }
}
