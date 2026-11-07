import { Injectable, signal } from '@angular/core';
import { Usuario } from '../models/usuario';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly _usuario = signal<Usuario>({
    id: 1,
    nombre: 'Vicente Pérez',
    correo: 'vicente.perez@duocuc.cl',
  });

  readonly usuario = this._usuario.asReadonly();
}
