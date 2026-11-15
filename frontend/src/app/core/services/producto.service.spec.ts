import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Producto } from '../models/producto';
import { ProductoService } from './producto.service';

const PRODUCTOS: Producto[] = [
  {
    id: 1,
    nombre: 'Mouse',
    descripcion: 'Mouse inalámbrico',
    precio: 10000,
    stock: 5,
    categoria: 'Accesorios',
    imagen: '/img/productos/mouse.svg',
  },
  {
    id: 2,
    nombre: 'Monitor',
    descripcion: 'Monitor 24 pulgadas',
    precio: 100000,
    stock: 2,
    categoria: 'Computación',
    imagen: '/img/productos/monitor.svg',
  },
  {
    id: 3,
    nombre: 'Teclado',
    descripcion: 'Teclado mecánico',
    precio: 20000,
    stock: 3,
    categoria: 'Accesorios',
    imagen: '/img/productos/teclado.svg',
  },
  {
    id: 4,
    nombre: 'Audífonos',
    descripcion: 'Audífonos Bluetooth',
    precio: 30000,
    stock: 4,
    categoria: 'Audio',
    imagen: '/img/productos/audifonos.svg',
  },
  {
    id: 5,
    nombre: 'Silla',
    descripcion: 'Silla ergonómica',
    precio: 50000,
    stock: 1,
    categoria: 'Hogar',
    imagen: '/img/productos/silla.svg',
  },
];

describe('ProductoService', () => {
  let servicio: ProductoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(ProductoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista los productos', () => {
    servicio.listar().subscribe((productos) => expect(productos).toEqual(PRODUCTOS));

    const peticion = http.expectOne('/api/productos');
    expect(peticion.request.method).toBe('GET');
    peticion.flush(PRODUCTOS);
  });

  it('obtiene un producto por id', () => {
    servicio.obtener(2).subscribe((producto) => expect(producto).toEqual(PRODUCTOS[1]));

    const peticion = http.expectOne('/api/productos/2');
    expect(peticion.request.method).toBe('GET');
    peticion.flush(PRODUCTOS[1]);
  });

  it('deriva las categorías sin duplicados', () => {
    servicio
      .categorias()
      .subscribe((categorias) =>
        expect(categorias).toEqual(['Accesorios', 'Computación', 'Audio', 'Hogar']),
      );

    http.expectOne('/api/productos').flush(PRODUCTOS);
  });

  it('toma los primeros cuatro productos como destacados', () => {
    servicio
      .destacados()
      .subscribe((productos) => expect(productos).toEqual(PRODUCTOS.slice(0, 4)));

    http.expectOne('/api/productos').flush(PRODUCTOS);
  });
});
