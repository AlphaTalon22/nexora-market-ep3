import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
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

  readonly metodosPago = [
    { valor: 'TARJETA_CREDITO', nombre: 'Tarjeta de crédito' },
    { valor: 'TARJETA_DEBITO', nombre: 'Tarjeta de débito' },
    { valor: 'TRANSFERENCIA', nombre: 'Transferencia bancaria' },
  ];

  private intervalo?: ReturnType<typeof setInterval>;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (Number.isNaN(id)) {
      this.cargando.set(false);
      return;
    }

    this.pedidos.obtener(id).subscribe((pedido) => {
      this.pedido.set(pedido ?? null);
      this.cargando.set(false);
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
      .procesar({ pedidoId: pedido.id, monto: pedido.total, metodoPago: this.metodoPago() })
      .subscribe(() => {
        this.toast.exito('Pago aprobado. Esperando la confirmación del pedido...');
        this.iniciarPolling(pedido.id);
      });
  }

  private iniciarPolling(id: number): void {
    this.detenerPolling();
    this.intervalo = setInterval(() => {
      this.pedidos.obtener(id).subscribe((pedido) => {
        if (pedido) {
          this.pedido.set(pedido);
        }
        if (pedido?.estado === 'PAGADO') {
          this.detenerPolling();
          this.pagando.set(false);
          this.toast.exito('Pedido confirmado como PAGADO');
        }
      });
    }, 2000);
  }

  private detenerPolling(): void {
    if (this.intervalo) {
      clearInterval(this.intervalo);
      this.intervalo = undefined;
    }
  }
}
