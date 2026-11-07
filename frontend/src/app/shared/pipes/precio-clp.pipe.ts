import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'precioClp' })
export class PrecioClpPipe implements PipeTransform {
  private readonly formato = new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    maximumFractionDigits: 0,
  });

  transform(valor: number | null | undefined): string {
    return valor == null ? '' : this.formato.format(valor);
  }
}
