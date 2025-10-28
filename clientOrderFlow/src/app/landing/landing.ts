import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './landing.html',
  styleUrls: ['./landing.css']
})
export class Landing {
  showLoginModal = false;
  showRegisterModal = false;

  loginEmail = '';
  loginPassword = '';
  loginRememberMe = false;

  registerEmail = '';
  registerPassword = '';
  registerConfirmPassword = '';
  registerCompanyName = '';
  registerUserType: 'supplier' | 'retail' | null = null;
  registerAcceptTerms = false;

  constructor(private router: Router) {}

  openLoginModal() {
    this.router.navigate(['/login']);
  }

  closeLoginModal() {
    this.showLoginModal = false;
  }

  openRegisterModal() {
    // Перенаправляем на страницу регистрации поставщика, где первый шаг - выбор типа
    this.router.navigate(['/supplier/auth/registration']);
  }

  closeRegisterModal() {
    this.showRegisterModal = false;
  }

  switchToRegister() {
    this.closeLoginModal();
    this.openRegisterModal();
  }

  switchToLogin() {
    this.closeRegisterModal();
    this.openLoginModal();
  }

  onLogin() {
    console.log('Login attempt:', { email: this.loginEmail, password: this.loginPassword });

    if (this.loginEmail && this.loginPassword) {
      if (this.loginEmail.includes('admin')) {
        this.router.navigate(['/admin/dashboard']);
      } else if (this.loginEmail.includes('supplier') || this.loginEmail.includes('поставщик')) {
        this.router.navigate(['/supplier/dashboard']);
      } else {
        this.router.navigate(['/retail/dashboard']);
      }
      this.closeLoginModal();
    } else {
      alert('Пожалуйста, заполните все поля');
    }
  }

  onRegister() {
    console.log('Register attempt:', {
      email: this.registerEmail,
      companyName: this.registerCompanyName,
      userType: this.registerUserType
    });

    if (!this.registerEmail || !this.registerPassword || !this.registerConfirmPassword ||
        !this.registerCompanyName || !this.registerUserType) {
      alert('Пожалуйста, заполните все поля');
      return;
    }

    if (this.registerPassword !== this.registerConfirmPassword) {
      alert('Пароли не совпадают');
      return;
    }

    if (!this.registerAcceptTerms) {
      alert('Необходимо принять условия использования');
      return;
    }

    if (this.registerUserType === 'supplier') {
      this.router.navigate(['/supplier/dashboard']);
    } else {
      this.router.navigate(['/retail/dashboard']);
    }
    this.closeRegisterModal();
  }
}
