import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./auth/login/login').then(m => m.Login)
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },
  {
    path: '',
    loadComponent: () => import('./shared/layout/layout').then(m => m.Layout),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'analytics',
        loadComponent: () => import('./analytics/analytics').then(m => m.Analytics)
      },
      {
        path: 'verification',
        loadComponent: () => import('./verification/verification-list').then(m => m.VerificationList)
      },
      {
        path: 'verification/:id',
        loadComponent: () => import('./verification/verification').then(m => m.Verification)
      },
      {
        path: 'support',
        loadComponent: () => import('./support/support').then(m => m.Support)
      },
      {
        path: 'support/:id',
        loadComponent: () => import('./support/support-ticket').then(m => m.SupportTicket)
      },
      {
        path: 'notifications',
        loadComponent: () => import('./notifications/notifications').then(m => m.Notifications)
      },
      {
        path: 'users',
        loadComponent: () => import('./users/users').then(m => m.Users)
      },
      {
        path: 'users/:id',
        loadComponent: () => import('./users/user-detail/user-detail').then(m => m.UserDetail)
      },
      {
        path: 'content',
        loadComponent: () => import('./content/content').then(m => m.Content)
      },
      {
        path: 'dictionaries',
        loadComponent: () => import('./dictionaries/dictionaries').then(m => m.Dictionaries)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];
