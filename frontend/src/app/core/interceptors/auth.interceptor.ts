import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);

  const vaAlApi = peticion.url.startsWith('/api') || peticion.url.startsWith(environment.apiBaseUrl);

  const respuesta$ = vaAlApi
    ? auth
        .obtenerToken()
        .pipe(
          switchMap((token) =>
            siguiente(
              token
                ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
                : peticion,
            ),
          ),
        )
    : siguiente(peticion);

  return respuesta$.pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
        toast.error('Tu sesión expiró o no tienes permisos. Inicia sesión nuevamente.');
      }
      return throwError(() => error);
    }),
  );
};
