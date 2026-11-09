import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { Layout } from './layout/layout';

export const routes: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: '',
        loadComponent: () => import('./features/home/home').then((m) => m.Home),
      },
      {
        path: 'catalogo',
        loadComponent: () => import('./features/catalogo/catalogo').then((m) => m.Catalogo),
      },
      {
        path: 'carrito',
        loadComponent: () => import('./features/carrito/carrito').then((m) => m.Carrito),
      },
      {
        path: 'pedidos',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/pedidos/lista-pedidos').then((m) => m.ListaPedidos),
      },
      {
        path: 'pedidos/:id',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/pedidos/detalle-pedido').then((m) => m.DetallePedido),
      },
      {
        path: 'cuenta',
        canActivate: [authGuard],
        loadComponent: () => import('./features/cuenta/cuenta').then((m) => m.Cuenta),
      },
      {
        path: 'dashboard',
        canActivate: [authGuard],
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'admin-kafka',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/en-construccion/en-construccion').then((m) => m.EnConstruccion),
        data: {
          titulo: 'Administración de Kafka',
          descripcion:
            'Tópicos, particiones, consumer groups y DLT del clúster — se habilitará próximamente.',
        },
      },
      {
        path: 'admin-rabbitmq',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/en-construccion/en-construccion').then((m) => m.EnConstruccion),
        data: {
          titulo: 'Administración de RabbitMQ',
          descripcion:
            'Colas, exchanges y bindings vía ms-rabbit-admin — se habilitará próximamente.',
        },
      },
      {
        path: '**',
        redirectTo: '',
      },
    ],
  },
];
