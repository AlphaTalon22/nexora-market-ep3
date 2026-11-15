import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpTestingController;
  let cliente: HttpClient;
  let baseOriginal: string;

  beforeEach(() => {
    baseOriginal = environment.apiBaseUrl;
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { obtenerToken: () => of('token-de-prueba') } },
        { provide: ToastService, useValue: { error: () => undefined } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    cliente = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    environment.apiBaseUrl = baseOriginal;
    http.verify();
  });

  it('no toca las URL externas ni les agrega token', () => {
    environment.apiBaseUrl = 'https://gateway.test';

    cliente.get('https://graph.microsoft.com/v1.0/me').subscribe();

    const peticion = http.expectOne('https://graph.microsoft.com/v1.0/me');
    expect(peticion.request.headers.has('Authorization')).toBe(false);
    peticion.flush({});
  });

  it('antepone apiBaseUrl sin barra final y agrega el token', () => {
    environment.apiBaseUrl = 'https://gateway.test/';

    cliente.get('/api/productos').subscribe();

    const peticion = http.expectOne('https://gateway.test/api/productos');
    expect(peticion.request.headers.get('Authorization')).toBe('Bearer token-de-prueba');
    peticion.flush([]);
  });

  it('mantiene el token en una URL ya prefijada', () => {
    environment.apiBaseUrl = 'https://gateway.test/prod';
    const url = 'https://gateway.test/prod/api/pedidos';

    cliente.get(url).subscribe();

    const peticion = http.expectOne(url);
    expect(peticion.request.headers.get('Authorization')).toBe('Bearer token-de-prueba');
    peticion.flush([]);
  });
});
