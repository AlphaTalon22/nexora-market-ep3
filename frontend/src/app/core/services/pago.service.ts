import { Injectable, inject } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { CrearPagoRequest, Pago } from '../models/pago';
import { PedidoService } from './pedido.service';

const CLAVE_ALMACENAMIENTO = 'nexora.pagos';
const RETARDO_EVENTO_MS = 2500;

@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly pedidos = inject(PedidoService);
  private pagos: Pago[] = this.leer();

  listar(): Observable<Pago[]> {
    return of([...this.pagos]).pipe(delay(250));
  }

  procesar(datos: CrearPagoRequest): Observable<Pago> {
    const pedido = this.pedidos.buscar(datos.pedidoId);

    const pago: Pago = {
      id: this.siguienteId(),
      pedidoId: datos.pedidoId,
      monto: pedido?.total ?? 0,
      metodoPago: datos.metodoPago,
      ultimos4: datos.ultimos4,
      estado: 'APROBADO',
      fechaPago: new Date().toISOString(),
    };

    this.pagos = [pago, ...this.pagos];
    this.guardar();

    setTimeout(() => this.pedidos.cambiarEstado(pago.pedidoId, 'PAGADO'), RETARDO_EVENTO_MS);
    return of(pago).pipe(delay(600));
  }

  private siguienteId(): number {
    return this.pagos.reduce((maximo, pago) => Math.max(maximo, pago.id), 0) + 1;
  }

  private leer(): Pago[] {
    try {
      const crudo = localStorage.getItem(CLAVE_ALMACENAMIENTO);
      if (crudo) {
        return JSON.parse(crudo) as Pago[];
      }
    } catch {
      localStorage.removeItem(CLAVE_ALMACENAMIENTO);
    }

    return [
      {
        id: 1,
        pedidoId: 2,
        monto: 74980,
        metodoPago: 'TARJETA_CREDITO',
        ultimos4: '4242',
        estado: 'APROBADO',
        fechaPago: new Date(Date.now() - 86_400_000).toISOString(),
      },
    ];
  }

  private guardar(): void {
    localStorage.setItem(CLAVE_ALMACENAMIENTO, JSON.stringify(this.pagos));
  }
}
