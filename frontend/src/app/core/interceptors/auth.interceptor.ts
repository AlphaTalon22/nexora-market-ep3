import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);

  const base = environment.apiBaseUrl.replace(/\/$/, '');
  const esApi = peticion.url.startsWith('/api');
  const url = esApi && base ? `${base}${peticion.url}` : peticion.url;
  const vaAlApi =
    esApi || (base !== '' && (peticion.url === base || peticion.url.startsWith(`${base}/`)));
  const peticionApi = peticion.clone({ url });

  const respuesta$ = vaAlApi
    ? auth
        .obtenerToken()
        .pipe(
          switchMap((token) =>
            siguiente(
              token
                ? peticionApi.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
                : peticionApi,
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
