import { Injectable } from '@angular/core';
import { firstValueFrom, forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { CatalogService } from './catalog.service';

@Injectable({
  providedIn: 'root'
})
export class AppBootstrapService {
  constructor(
    private authService: AuthService,
    private catalogService: CatalogService
  ) {}

  async init(): Promise<void> {
    if (!this.authService.hasValidSession()) {
      return;
    }

    await firstValueFrom(
      this.authService.refreshProfile().pipe(
        catchError(error => {
          console.error('App bootstrap auth refresh error:', error);
          return of(null);
        }),
        switchMap(profile => {
          const role = profile?.role ?? this.authService.getUserRole();
          if (role !== 'SUPPLIER' && role !== 'RETAIL_CHAIN') {
            return of(void 0);
          }

          return forkJoin([
            this.catalogService.getCategoryTree().pipe(catchError(() => of([]))),
            this.catalogService.getUnits().pipe(catchError(() => of([]))),
            this.catalogService.getVatRates().pipe(catchError(() => of([])))
          ]).pipe(map(() => void 0));
        })
      )
    );
  }
}

