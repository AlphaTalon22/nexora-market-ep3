import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly snackBar = inject(MatSnackBar);

  exito(mensaje: string): void {
    this.snackBar.open(mensaje, 'Cerrar', {
      duration: 3500,
      panelClass: 'toast-exito',
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
    });
  }

  error(mensaje: string): void {
    this.snackBar.open(mensaje, 'Cerrar', {
      duration: 4500,
      panelClass: 'toast-error',
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
    });
  }
}
