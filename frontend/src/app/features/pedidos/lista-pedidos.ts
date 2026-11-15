import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { Pedido } from '../../core/models/pedido';
import { PedidoService } from '../../core/services/pedido.service';
import { ToastService } from '../../core/services/toast.service';
import { EstadoBadge } from '../../shared/estado-badge/estado-badge';
import { PrecioClpPipe } from '../../shared/pipes/precio-clp.pipe';

@Component({
  selector: 'app-lista-pedidos',
  imports: [RouterLink, DatePipe, MatButton, MatIcon, EstadoBadge, PrecioClpPipe],
  templateUrl: './lista-pedidos.html',
  styleUrl: './lista-pedidos.css',
})
export class ListaPedidos {
  private readonly pedidoService = inject(PedidoService);
  private readonly toast = inject(ToastService);

  readonly cargando = signal(true);
  readonly pedidos = signal<Pedido[]>([]);

  constructor() {
    this.pedidoService.listar().subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.toast.error('No se pudieron cargar los pedidos.');
      },
    });
  }
}
