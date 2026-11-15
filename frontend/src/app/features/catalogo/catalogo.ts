import { Component, computed, inject, signal } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatOption, MatSelect } from '@angular/material/select';
import { ActivatedRoute } from '@angular/router';
import { Producto } from '../../core/models/producto';
import { CarritoService } from '../../core/services/carrito.service';
import { ProductoService } from '../../core/services/producto.service';
import { ToastService } from '../../core/services/toast.service';
import { ProductoCard } from '../../shared/producto-card/producto-card';

@Component({
  selector: 'app-catalogo',
  imports: [
    MatButton,
    MatFormField,
    MatLabel,
    MatInput,
    MatSelect,
    MatOption,
    MatIcon,
    ProductoCard,
  ],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
})
export class Catalogo {
  private readonly productoService = inject(ProductoService);
  private readonly carrito = inject(CarritoService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);

  readonly cargando = signal(true);
  readonly productos = signal<Producto[]>([]);
  readonly busqueda = signal('');
  readonly categoria = signal('Todas');
  readonly categorias = signal<string[]>(['Todas']);

  readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const categoria = this.categoria();

    return this.productos().filter((producto) => {
      const coincideCategoria = categoria === 'Todas' || producto.categoria === categoria;
      const coincideTexto =
        texto === '' ||
        producto.nombre.toLowerCase().includes(texto) ||
        (producto.descripcion ?? '').toLowerCase().includes(texto);
      return coincideCategoria && coincideTexto;
    });
  });

  constructor() {
    const categoriaInicial = this.route.snapshot.queryParamMap.get('categoria');

    this.productoService.categorias().subscribe({
      next: (categorias) => {
        this.categorias.set(['Todas', ...categorias]);
        if (
          categoriaInicial &&
          categorias.includes(categoriaInicial) &&
          this.categoria() === 'Todas'
        ) {
          this.categoria.set(categoriaInicial);
        }
      },
      error: () => this.toast.error('No se pudieron cargar las categorías.'),
    });

    this.productoService.listar().subscribe({
      next: (productos) => {
        this.productos.set(productos);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.toast.error('No se pudo cargar el catálogo.');
      },
    });
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.categoria.set('Todas');
  }

  agregar(producto: Producto): void {
    this.carrito.agregar(producto);
    this.toast.exito(`${producto.nombre} agregado al carrito`);
  }
}
