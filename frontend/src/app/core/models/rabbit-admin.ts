export interface NodoRabbit {
  nombre: string;
  estado: string;
}

export interface ColaRabbit {
  nombre: string;
  mensajes: number;
  consumidores: number;
  dlq?: string;
}

export interface ExchangeRabbit {
  nombre: string;
  tipo: string;
}

export interface BindingRabbit {
  exchange: string;
  cola: string;
  routingKey: string;
}
