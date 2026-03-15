import { Injectable } from '@angular/core';
import { CanActivate, CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';
import { UserRole } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    const requiredRoles = (route.data['roles'] as UserRole[] | undefined) ?? [];
    if (requiredRoles.length === 0 || this.authService.hasAnyRole(requiredRoles)) {
      return true;
    }

    this.router.navigateByUrl(this.authService.getDefaultRoute());
    return false;
  }
}

/**
 * Functional guard version
 */
export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  const requiredRoles = (route.data['roles'] as UserRole[] | undefined) ?? [];
  if (requiredRoles.length === 0 || authService.hasAnyRole(requiredRoles)) {
    return true;
  }

  router.navigateByUrl(authService.getDefaultRoute());
  return false;
};
