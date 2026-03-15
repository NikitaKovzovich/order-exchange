import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const publicUrls = [
    '/api/auth/register',
    '/api/auth/login'
  ];

  const isPublicUrl = publicUrls.some(url => req.url.includes(url));

  const token = authService.getToken();

  if (token && !isPublicUrl) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      },
      headers: req.headers
        .delete('X-User-Email')
        .delete('X-User-Id')
        .delete('X-User-Role')
        .delete('X-User-Company-Id')
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isPublicUrl) {
        authService.logout();
        router.navigate(['/login'], {
          queryParams: { returnUrl: router.url }
        });
      }

      return throwError(() => error);
    })
  );
};
