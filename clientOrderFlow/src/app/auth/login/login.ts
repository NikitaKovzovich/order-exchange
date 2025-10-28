import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  email: string = '';
  password: string = '';
  rememberMe: boolean = false;

  constructor(private router: Router) {}

  onSubmit() {
    console.log('Login attempt:', { email: this.email, password: this.password });

    if (this.email && this.password) {
      if (this.email.includes('admin')) {
        this.router.navigate(['/admin/dashboard']);
      } else if (this.email.includes('supplier') || this.email.includes('поставщик')) {
        this.router.navigate(['/supplier/dashboard']);
      } else {
        this.router.navigate(['/retail/dashboard']);
      }
    } else {
      alert('Пожалуйста, заполните все поля');
    }
  }
}

