import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Producto } from '../models/producto';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>('/api/productos');
  }

  obtener(id: number): Observable<Producto> {
    return this.http.get<Producto>(`/api/productos/${id}`);
  }

  categorias(): Observable<string[]> {
    return this.listar().pipe(
      map((productos) => [...new Set(productos.map((producto) => producto.categoria))]),
    );
  }

  destacados(): Observable<Producto[]> {
    return this.listar().pipe(map((productos) => productos.slice(0, 4)));
  }
}
