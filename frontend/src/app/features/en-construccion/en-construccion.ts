import { Component, input } from '@angular/core';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-en-construccion',
  imports: [MatIcon],
  template: `
    <div class="estado-vacio">
      <mat-icon>construction</mat-icon>
      <h3>{{ titulo() }}</h3>
      <p>{{ descripcion() }}</p>
    </div>
  `,
})
export class EnConstruccion {
  readonly titulo = input('Módulo en desarrollo');
  readonly descripcion = input('Este módulo se habilitará en la Fase 5.');
}
