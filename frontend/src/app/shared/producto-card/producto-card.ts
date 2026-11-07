import { Component, input, output } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatCard } from '@angular/material/card';
import { MatIcon } from '@angular/material/icon';
import { Producto } from '../../core/models/producto';
import { PrecioClpPipe } from '../pipes/precio-clp.pipe';

@Component({
  selector: 'app-producto-card',
  imports: [MatButton, MatCard, MatIcon, PrecioClpPipe],
  templateUrl: './producto-card.html',
  styleUrl: './producto-card.css',
})
export class ProductoCard {
  readonly producto = input.required<Producto>();
  readonly agregar = output<Producto>();
}
