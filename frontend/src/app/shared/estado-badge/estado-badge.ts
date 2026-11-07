import { Component, computed, input } from '@angular/core';
import { EstadoPedido } from '../../core/models/pedido';

const ETIQUETAS: Record<EstadoPedido, string> = {
  PENDIENTE: 'Pendiente',
  PAGADO: 'Pagado',
  RECHAZADO: 'Rechazado',
  CANCELADO: 'Cancelado',
};

@Component({
  selector: 'app-estado-badge',
  template: `<span class="badge" [class]="clase()">{{ etiqueta() }}</span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 4px 12px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.3px;
    }
    .badge-pendiente {
      background: #fef3c7;
      color: #92400e;
    }
    .badge-pagado {
      background: #dcfce7;
      color: #166534;
    }
    .badge-rechazado {
      background: #fee2e2;
      color: #991b1b;
    }
    .badge-cancelado {
      background: #e5e7eb;
      color: #374151;
    }
  `,
})
export class EstadoBadge {
  readonly estado = input<EstadoPedido>('PENDIENTE');

  readonly etiqueta = computed(() => ETIQUETAS[this.estado()]);
  readonly clase = computed(() => `badge badge-${this.estado().toLowerCase()}`);
}
