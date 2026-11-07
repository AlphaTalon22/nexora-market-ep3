import { TestBed } from '@angular/core/testing';
import { Producto } from '../models/producto';
import { CarritoService } from './carrito.service';

const PRODUCTO: Producto = {
  id: 99,
  nombre: 'Producto de prueba',
  descripcion: 'Descripción de prueba',
  precio: 10000,
  stock: 5,
  categoria: 'Pruebas',
  imagen: '/img/productos/mouse.svg',
};

describe('CarritoService', () => {
  let servicio: CarritoService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    servicio = TestBed.inject(CarritoService);
  });

  it('parte vacío', () => {
    expect(servicio.items().length).toBe(0);
    expect(servicio.cantidadTotal()).toBe(0);
    expect(servicio.total()).toBe(0);
  });

  it('agrega un producto y calcula totales', () => {
    servicio.agregar(PRODUCTO, 2);

    expect(servicio.items().length).toBe(1);
    expect(servicio.cantidadTotal()).toBe(2);
    expect(servicio.total()).toBe(20000);
  });

  it('acumula cantidades del mismo producto sin superar el stock', () => {
    servicio.agregar(PRODUCTO, 3);
    servicio.agregar(PRODUCTO, 4);

    expect(servicio.items().length).toBe(1);
    expect(servicio.cantidadTotal()).toBe(5);
  });

  it('cambia la cantidad y elimina al llegar a cero', () => {
    servicio.agregar(PRODUCTO, 2);
    servicio.cambiarCantidad(PRODUCTO.id, 4);
    expect(servicio.cantidadTotal()).toBe(4);

    servicio.cambiarCantidad(PRODUCTO.id, 0);
    expect(servicio.items().length).toBe(0);
  });

  it('persiste el carrito en localStorage', () => {
    servicio.agregar(PRODUCTO, 1);

    const crudo = localStorage.getItem('nexora.carrito');
    expect(crudo).toContain('Producto de prueba');
  });

  it('vacía el carrito', () => {
    servicio.agregar(PRODUCTO, 1);
    servicio.vaciar();

    expect(servicio.items().length).toBe(0);
    expect(servicio.total()).toBe(0);
  });
});
