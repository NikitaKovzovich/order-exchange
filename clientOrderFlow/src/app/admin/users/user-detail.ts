import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';

@Component({
  selector: 'admin-user-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, Header],
  templateUrl: './user-detail.html',
  styleUrls: ['./user-detail.css']
})
export class UserDetail implements OnInit {
  userId: string = '';
  user: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.userId = this.route.snapshot.paramMap.get('id') || '';
    this.loadUser();
  }

  loadUser() {
    this.user = {
      id: this.userId,
      name: 'ООО "Торговый дом"',
      email: 'td@example.com',
      phone: '+375 29 123-45-67',
      role: 'retail',
      status: 'active',
      registeredAt: '2024-01-15',
      inn: '1234567890',
      address: 'г. Минск, ул. Примерная, 1'
    };
  }

  blockUser() {
    if (confirm('Вы уверены, что хотите заблокировать пользователя?')) {
      alert('Пользователь заблокирован');
    }
  }

  deleteUser() {
    if (confirm('Вы уверены, что хотите удалить пользователя? Это действие необратимо.')) {
      this.router.navigate(['/admin/users']);
    }
  }
}
