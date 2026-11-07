import { Injectable, computed, signal } from '@angular/core';
import { ItemCarrito } from '../models/item-carrito';
import { Producto } from '../models/producto';

const CLAVE_ALMACENAMIENTO = 'nexora.carrito';

@Injectable({ providedIn: 'root' })
export class CarritoService {
  private readonly _items = signal<ItemCarrito[]>(this.leer());

  readonly items = this._items.asReadonly();

  readonly cantidadTotal = computed(() =>
    this._items().reduce((total, item) => total + item.cantidad, 0),
  );

  readonly total = computed(() =>
    this._items().reduce((total, item) => total + item.precioUnitario * item.cantidad, 0),
  );

  agregar(producto: Producto, cantidad = 1): void {
    const items = this._items();
    const existente = items.find((item) => item.productoId === producto.id);

    if (existente) {
      const nuevaCantidad = Math.min(existente.cantidad + cantidad, producto.stock);
      this._items.set(
        items.map((item) =>
          item.productoId === producto.id ? { ...item, cantidad: nuevaCantidad } : item,
        ),
      );
    } else {
      this._items.set([
        ...items,
        {
          productoId: producto.id,
          nombreProducto: producto.nombre,
          precioUnitario: producto.precio,
          cantidad: Math.min(cantidad, producto.stock),
          imagen: producto.imagen,
        },
      ]);
    }

    this.guardar();
  }

  cambiarCantidad(productoId: number, cantidad: number): void {
    if (cantidad <= 0) {
      this.quitar(productoId);
      return;
    }

    this._items.set(
      this._items().map((item) => (item.productoId === productoId ? { ...item, cantidad } : item)),
    );
    this.guardar();
  }

  quitar(productoId: number): void {
    this._items.set(this._items().filter((item) => item.productoId !== productoId));
    this.guardar();
  }

  vaciar(): void {
    this._items.set([]);
    this.guardar();
  }

  private leer(): ItemCarrito[] {
    try {
      const crudo = localStorage.getItem(CLAVE_ALMACENAMIENTO);
      return crudo ? (JSON.parse(crudo) as ItemCarrito[]) : [];
    } catch {
      return [];
    }
  }

  private guardar(): void {
    localStorage.setItem(CLAVE_ALMACENAMIENTO, JSON.stringify(this._items()));
  }
}
