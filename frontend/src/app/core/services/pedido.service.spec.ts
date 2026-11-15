import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CrearPedidoRequest, Pedido } from '../models/pedido';
import { PedidoService } from './pedido.service';

const PEDIDO: Pedido = {
  id: 7,
  clienteId: 'oid-123',
  fecha: '2026-11-15T12:00:00',
  total: 30000,
  estado: 'PENDIENTE',
  items: [
    {
      productoId: 1,
      nombreProducto: 'Mouse',
      precioUnitario: 10000,
      cantidad: 3,
    },
  ],
};

describe('PedidoService', () => {
  let servicio: PedidoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(PedidoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista los pedidos', () => {
    servicio.listar().subscribe((pedidos) => expect(pedidos).toEqual([PEDIDO]));

    const peticion = http.expectOne('/api/pedidos');
    expect(peticion.request.method).toBe('GET');
    peticion.flush([PEDIDO]);
  });

  it('obtiene un pedido por id', () => {
    servicio.obtener(7).subscribe((pedido) => expect(pedido).toEqual(PEDIDO));

    const peticion = http.expectOne('/api/pedidos/7');
    expect(peticion.request.method).toBe('GET');
    peticion.flush(PEDIDO);
  });

  it('crea un pedido con el contrato de items', () => {
    const datos: CrearPedidoRequest = { items: [{ productoId: 1, cantidad: 3 }] };

    servicio.crear(datos).subscribe((pedido) => expect(pedido).toEqual(PEDIDO));

    const peticion = http.expectOne('/api/pedidos');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(datos);
    peticion.flush(PEDIDO);
  });

  it('cancela un pedido', () => {
    const cancelado: Pedido = { ...PEDIDO, estado: 'CANCELADO' };

    servicio.cancelar(7).subscribe((pedido) => expect(pedido).toEqual(cancelado));

    const peticion = http.expectOne('/api/pedidos/7/cancelar');
    expect(peticion.request.method).toBe('POST');
    peticion.flush(cancelado);
  });

  it('propaga el 409 con el mensaje del backend', () => {
    const datos: CrearPedidoRequest = { items: [{ productoId: 1, cantidad: 99 }] };
    let mensaje = '';

    servicio.crear(datos).subscribe({
      error: (error) => (mensaje = error.error.error),
    });

    http
      .expectOne('/api/pedidos')
      .flush({ error: 'Stock insuficiente' }, { status: 409, statusText: 'Conflict' });

    expect(mensaje).toBe('Stock insuficiente');
  });
});
