import { Routes } from '@angular/router';
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
        loadComponent: () =>
          import('./features/pedidos/lista-pedidos').then((m) => m.ListaPedidos),
      },
      {
        path: 'pedidos/:id',
        loadComponent: () =>
          import('./features/pedidos/detalle-pedido').then((m) => m.DetallePedido),
      },
      {
        path: 'cuenta',
        loadComponent: () => import('./features/cuenta/cuenta').then((m) => m.Cuenta),
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/en-construccion/en-construccion').then((m) => m.EnConstruccion),
        data: {
          titulo: 'Dashboard en tiempo real',
          descripcion:
            'Se habilitará en la Fase 5 con métricas de Kafka y el microservicio de analítica.',
        },
      },
      {
        path: 'admin-kafka',
        loadComponent: () =>
          import('./features/en-construccion/en-construccion').then((m) => m.EnConstruccion),
        data: {
          titulo: 'Administración de Kafka',
          descripcion:
            'Tópicos, particiones, consumer groups y DLT del clúster — se habilitará en la Fase 5.',
        },
      },
      {
        path: 'admin-rabbitmq',
        loadComponent: () =>
          import('./features/en-construccion/en-construccion').then((m) => m.EnConstruccion),
        data: {
          titulo: 'Administración de RabbitMQ',
          descripcion:
            'Colas, exchanges y bindings vía ms-rabbit-admin — se habilitará en la Fase 5.',
        },
      },
      {
        path: '**',
        redirectTo: '',
      },
    ],
  },
];
