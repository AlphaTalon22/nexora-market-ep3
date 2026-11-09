import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);

  if (auth.autenticado()) {
    return true;
  }

  inject(ToastService).error('Inicia sesión para acceder a esta sección.');
  auth.login();
  return false;
};
