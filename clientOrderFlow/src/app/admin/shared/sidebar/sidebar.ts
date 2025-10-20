import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'admin-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css']
})
export class Sidebar {
  constructor(private router: Router) {}

  isActive(route: string): boolean {
    return this.router.url.includes(route);
  }

  logout() {
    this.router.navigate(['/admin/auth/login']);
  }
}

