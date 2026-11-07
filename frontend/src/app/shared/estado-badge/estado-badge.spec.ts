import { TestBed } from '@angular/core/testing';
import { EstadoBadge } from './estado-badge';

describe('EstadoBadge', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstadoBadge],
    }).compileComponents();
  });

  it('muestra la etiqueta Pendiente', async () => {
    const fixture = TestBed.createComponent(EstadoBadge);
    fixture.componentRef.setInput('estado', 'PENDIENTE');
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Pendiente');
    expect(fixture.nativeElement.querySelector('.badge-pendiente')).toBeTruthy();
  });

  it('muestra la etiqueta Pagado con su clase', async () => {
    const fixture = TestBed.createComponent(EstadoBadge);
    fixture.componentRef.setInput('estado', 'PAGADO');
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Pagado');
    expect(fixture.nativeElement.querySelector('.badge-pagado')).toBeTruthy();
  });
});
