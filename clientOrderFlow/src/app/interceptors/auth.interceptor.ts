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
  const currentUser = authService.getCurrentUser();

  if (token && !isPublicUrl) {
    const headers: { [key: string]: string } = {
      Authorization: `Bearer ${token}`
    };

    if (currentUser?.email) {
      headers['X-User-Email'] = currentUser.email;
    }

    if (currentUser?.userId) {
      headers['X-User-Id'] = currentUser.userId.toString();
    }

    if (currentUser?.role) {
      headers['X-User-Role'] = currentUser.role;
    }

    if (currentUser?.companyId) {
      headers['X-User-Company-Id'] = currentUser.companyId.toString();
    }

    req = req.clone({ setHeaders: headers });
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
