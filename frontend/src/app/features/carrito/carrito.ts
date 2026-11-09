import { Component, inject, signal } from '@angular/core';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatDivider } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { Router, RouterLink } from '@angular/router';
import { ItemCarrito } from '../../core/models/item-carrito';
import { AuthService } from '../../core/services/auth.service';
import { CarritoService } from '../../core/services/carrito.service';
import { PedidoService } from '../../core/services/pedido.service';
import { ToastService } from '../../core/services/toast.service';
import { PrecioClpPipe } from '../../shared/pipes/precio-clp.pipe';

@Component({
  selector: 'app-carrito',
  imports: [RouterLink, MatButton, MatIconButton, MatDivider, MatIcon, PrecioClpPipe],
  templateUrl: './carrito.html',
  styleUrl: './carrito.css',
})
export class Carrito {
  readonly carrito = inject(CarritoService);
  private readonly pedidos = inject(PedidoService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly confirmando = signal(false);

  aumentar(item: ItemCarrito): void {
    this.carrito.cambiarCantidad(item.productoId, item.cantidad + 1);
  }

  disminuir(item: ItemCarrito): void {
    this.carrito.cambiarCantidad(item.productoId, item.cantidad - 1);
  }

  quitar(item: ItemCarrito): void {
    this.carrito.quitar(item.productoId);
    this.toast.exito(`${item.nombreProducto} eliminado del carrito`);
  }

  confirmarPedido(): void {
    if (this.carrito.items().length === 0 || this.confirmando()) {
      return;
    }

    if (!this.auth.autenticado()) {
      this.toast.error('Debes iniciar sesión para confirmar el pedido.');
      this.auth.login();
      return;
    }

    this.confirmando.set(true);
    this.pedidos
      .crear({
        items: this.carrito.items().map((item) => ({
          productoId: item.productoId,
          cantidad: item.cantidad,
        })),
      })
      .subscribe((pedido) => {
        this.confirmando.set(false);
        this.carrito.vaciar();
        this.toast.exito(`Pedido #${pedido.id} creado correctamente`);
        this.router.navigate(['/pedidos', pedido.id]);
      });
  }
}
