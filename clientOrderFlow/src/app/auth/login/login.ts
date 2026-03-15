import {Component, OnInit} from '@angular/core';
import {Router, RouterLink, ActivatedRoute} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {AuthService} from '../../services/auth.service';
import {LoginRequest} from '../../models/api.models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit {
  email: string = '';
  password: string = '';
  rememberMe: boolean = false;
  isLoading: boolean = false;
  errorMessage: string = '';
  returnUrl: string = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    if (this.authService.isAuthenticated()) {
      this.router.navigateByUrl(this.authService.getDefaultRoute());
    }
  }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '';
  }

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Пожалуйста, заполните все поля';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const credentials: LoginRequest = {
      email: this.email,
      password: this.password
    };

    this.authService.login(credentials, this.rememberMe).subscribe({
      next: (response) => {
        console.log('Login successful:', response);
        this.isLoading = false;

        if (this.returnUrl) {
          this.router.navigateByUrl(this.returnUrl);
          return;
        }

        this.router.navigateByUrl(this.authService.getDefaultRouteForRole(response.role));
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Login error:', error);

        if (error.status === 401) {
          this.errorMessage = 'Неверный email или пароль';
        } else if (error.status === 400) {
          this.errorMessage = error.error?.message || error.error?.error || 'Ошибка в данных. Проверьте форму.';
        } else if (error.status === 0) {
          this.errorMessage = 'Ошибка подключения к серверу. Проверьте, что сервер запущен на http://localhost:8765';
        } else if (error.error?.error) {
          this.errorMessage = error.error.error;
        } else if (error.error?.message) {
          this.errorMessage = error.error.message;
        } else {
          this.errorMessage = 'Произошла ошибка при входе. Попробуйте снова.';
        }
      }
    });
  }
}
