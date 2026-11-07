import { Component, inject, signal } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { Producto } from '../../core/models/producto';
import { CarritoService } from '../../core/services/carrito.service';
import { ProductoService } from '../../core/services/producto.service';
import { ToastService } from '../../core/services/toast.service';
import { ProductoCard } from '../../shared/producto-card/producto-card';

const ICONOS_CATEGORIA: Record<string, string> = {
  Audio: 'headphones',
  Computación: 'computer',
  Smartphones: 'smartphone',
  Accesorios: 'cable',
  Gaming: 'sports_esports',
  Hogar: 'chair',
};

@Component({
  selector: 'app-home',
  imports: [RouterLink, MatButton, MatIcon, ProductoCard],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  private readonly productoService = inject(ProductoService);
  private readonly carrito = inject(CarritoService);
  private readonly toast = inject(ToastService);

  readonly cargando = signal(true);
  readonly destacados = signal<Producto[]>([]);
  readonly categorias = this.productoService.categorias();

  constructor() {
    this.productoService.destacados().subscribe((productos) => {
      this.destacados.set(productos);
      this.cargando.set(false);
    });
  }

  icono(categoria: string): string {
    return ICONOS_CATEGORIA[categoria] ?? 'category';
  }

  agregar(producto: Producto): void {
    this.carrito.agregar(producto);
    this.toast.exito(`${producto.nombre} agregado al carrito`);
  }
}
