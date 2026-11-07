import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { forkJoin } from 'rxjs';
import { Boleta } from '../../core/models/boleta';
import { Notificacion } from '../../core/models/notificacion';
import { NotificacionService } from '../../core/services/notificacion.service';
import { PagoService } from '../../core/services/pago.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { PrecioClpPipe } from '../../shared/pipes/precio-clp.pipe';

const ICONOS_NOTIFICACION: Record<Notificacion['tipo'], string> = {
  PEDIDO: 'shopping_bag',
  PAGO: 'payments',
  BOLETA: 'receipt_long',
};

@Component({
  selector: 'app-cuenta',
  imports: [DatePipe, MatIcon, PrecioClpPipe],
  templateUrl: './cuenta.html',
  styleUrl: './cuenta.css',
})
export class Cuenta {
  readonly usuario = inject(UsuarioService);
  private readonly notificacionService = inject(NotificacionService);
  private readonly pagoService = inject(PagoService);

  readonly cargando = signal(true);
  readonly notificaciones = signal<Notificacion[]>([]);
  readonly boletas = signal<Boleta[]>([]);

  readonly iniciales = computed(() => {
    const partes = this.usuario.usuario().nombre.split(' ');
    return partes
      .slice(0, 2)
      .map((parte) => parte.charAt(0))
      .join('')
      .toUpperCase();
  });

  constructor() {
    forkJoin({
      notificaciones: this.notificacionService.listar(),
      pagos: this.pagoService.listar(),
    }).subscribe(({ notificaciones, pagos }) => {
      this.notificaciones.set(notificaciones);
      this.boletas.set(
        pagos.map((pago) => ({
          pagoId: pago.id,
          pedidoId: pago.pedidoId,
          fecha: pago.fechaPago,
          metodoPago: pago.metodoPago,
          monto: pago.monto,
        })),
      );
      this.cargando.set(false);
    });
  }

  iconoNotificacion(tipo: Notificacion['tipo']): string {
    return ICONOS_NOTIFICACION[tipo];
  }
}
