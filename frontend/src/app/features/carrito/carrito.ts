import { Component, inject, signal } from '@angular/core';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatDivider } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { Router, RouterLink } from '@angular/router';
import { ItemCarrito } from '../../core/models/item-carrito';
import { CarritoService } from '../../core/services/carrito.service';
import { PedidoService } from '../../core/services/pedido.service';
import { ToastService } from '../../core/services/toast.service';
import { UsuarioService } from '../../core/services/usuario.service';
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
  private readonly usuario = inject(UsuarioService);
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

    this.confirmando.set(true);
    this.pedidos
      .crear({
        clienteId: this.usuario.usuario().id,
        total: this.carrito.total(),
        items: this.carrito.items(),
      })
      .subscribe((pedido) => {
        this.confirmando.set(false);
        this.carrito.vaciar();
        this.toast.exito(`Pedido #${pedido.id} creado correctamente`);
        this.router.navigate(['/pedidos', pedido.id]);
      });
  }
}
