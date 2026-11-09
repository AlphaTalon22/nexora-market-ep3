import { Injectable, computed, inject, signal } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { AccountInfo } from '@azure/msal-browser';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Usuario } from '../models/usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly msal = inject(MsalService);

  private readonly _usuario = signal<Usuario | null>(null);
  readonly usuario = this._usuario.asReadonly();
  readonly autenticado = computed(() => this._usuario() !== null);
  readonly cargando = signal(true);

  inicializar(): Observable<void> {
    return this.msal.initialize().pipe(
      switchMap(() => this.msal.handleRedirectObservable()),
      tap((resultado) => {
        if (resultado?.account) {
          this.msal.instance.setActiveAccount(resultado.account);
        }
        this.actualizarUsuario();
        this.cargando.set(false);
      }),
      map(() => undefined),
      catchError(() => {
        this.actualizarUsuario();
        this.cargando.set(false);
        return of(undefined);
      }),
    );
  }

  login(): void {
    this.msal.loginRedirect({ scopes: environment.auth.scopes }).subscribe();
  }

  crearCuenta(): void {
    this.msal.loginRedirect({ scopes: environment.auth.scopes, prompt: 'create' }).subscribe();
  }

  cerrarSesion(): void {
    const cuenta = this.cuentaActiva();
    this._usuario.set(null);
    this.msal
      .logoutRedirect({
        account: cuenta ?? undefined,
        postLogoutRedirectUri: environment.auth.postLogoutRedirectUri,
      })
      .subscribe();
  }

  obtenerToken(): Observable<string | null> {
    const cuenta = this.cuentaActiva();
    if (!cuenta) {
      return of(null);
    }

    return this.msal
      .acquireTokenSilent({ scopes: environment.auth.scopes, account: cuenta })
      .pipe(
        map((resultado) => resultado.accessToken),
        catchError(() => of(null)),
      );
  }

  private cuentaActiva(): AccountInfo | null {
    return this.msal.instance.getActiveAccount() ?? this.msal.instance.getAllAccounts()[0] ?? null;
  }

  private actualizarUsuario(): void {
    const cuenta = this.cuentaActiva();
    if (!cuenta) {
      this._usuario.set(null);
      return;
    }

    this.msal.instance.setActiveAccount(cuenta);
    const claims = (cuenta.idTokenClaims ?? {}) as Record<string, unknown>;
    const nombre =
      (typeof claims['name'] === 'string' ? claims['name'] : undefined) ?? cuenta.name ?? 'Usuario';
    const correo =
      (typeof claims['preferred_username'] === 'string' ? claims['preferred_username'] : undefined) ??
      (typeof claims['email'] === 'string' ? claims['email'] : undefined) ??
      cuenta.username;

    this._usuario.set({
      id: cuenta.localAccountId,
      nombre,
      correo,
    });
  }
}
