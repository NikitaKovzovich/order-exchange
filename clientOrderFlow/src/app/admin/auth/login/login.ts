import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  email: string = '';
  password: string = '';

  constructor(private router: Router) {}

  login() {
    if (this.email && this.password) {
      this.router.navigate(['/admin/dashboard']);
    } else {
      alert('Пожалуйста, заполните все поля');
    }
  }
}

