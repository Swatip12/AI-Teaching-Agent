import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import * as fc from 'fast-check';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have the 'AI Teaching Platform' title`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('AI Teaching Platform');
  });

  it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('AI Teaching Platform');
  });

  // Example property-based test using fast-check
  it('should handle title property correctly with any string input', () => {
    fc.assert(fc.property(fc.string(), (title) => {
      const fixture = TestBed.createComponent(AppComponent);
      const app = fixture.componentInstance;
      app.title = title;
      expect(app.title).toBe(title);
    }), { numRuns: 100 });
  });
});