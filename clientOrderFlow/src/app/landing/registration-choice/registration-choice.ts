import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registration-choice',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './registration-choice.html',
  styleUrl: './registration-choice.css'
})
export class RegistrationChoice {
  constructor(private router: Router) {}

  selectUserType(type: 'supplier' | 'retail') {
    if (type === 'supplier') {
      this.router.navigate(['/supplier/auth/registration']);
    } else {
      this.router.navigate(['/retail/auth/registration']);
    }
  }
}

