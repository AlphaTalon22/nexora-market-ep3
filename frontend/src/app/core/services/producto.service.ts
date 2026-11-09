import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { PRODUCTOS } from '../mock/datos-mock';
import { Producto } from '../models/producto';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  listar(): Observable<Producto[]> {
    return of(PRODUCTOS).pipe(delay(400));
  }

  obtener(id: number): Observable<Producto | undefined> {
    return of(PRODUCTOS.find((producto) => producto.id === id)).pipe(delay(200));
  }

  categorias(): string[] {
    return [...new Set(PRODUCTOS.map((producto) => producto.categoria))];
  }

  destacados(): Observable<Producto[]> {
    return of(PRODUCTOS.slice(0, 4)).pipe(delay(400));
  }
}
