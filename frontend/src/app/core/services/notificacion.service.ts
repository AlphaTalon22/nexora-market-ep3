import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Notificacion } from '../models/notificacion';

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>('/api/notificaciones');
  }
}
