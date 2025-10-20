import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Header } from '../../shared/header/header';

interface UserDetailData {
  id: number;
  name: string;
  fullName: string;
  taxId: string;
  email: string;
  phone: string;
  role: string;
  status: string;
  registeredAt: string;
  address: string;
  stats: {
    totalOrders: number;
    totalRevenue: string;
    totalClients: number;
    lastOrderDate: string;
  };
}

@Component({
  selector: 'admin-user-detail',
  standalone: true,
  imports: [CommonModule, Header],
  templateUrl: './user-detail.html',
  styleUrls: ['./user-detail.css']
})
export class UserDetail implements OnInit {
  user: UserDetailData | null = null;
  showBlockModal = false;
  showDeleteModal = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadUser(id);
  }

  loadUser(id: number) {
    this.user = {
      id: id,
      name: 'Продукты Оптом',
      fullName: 'Частное торговое унитарное предприятие "Продукты Оптом"',
      taxId: '191234567',
      email: 'contact@prodopt.by',
      phone: '+375 (29) 123-45-67',
      role: 'supplier',
      status: 'active',
      registeredAt: '15.10.2025',
      address: '220004, г. Минск, ул. Центральная, д. 1, оф. 101',
      stats: {
        totalOrders: 154,
        totalRevenue: '45,800',
        totalClients: 12,
        lastOrderDate: '16.10.2025'
      }
    };
  }

  blockUser() {
    console.log('Block user:', this.user?.id);
    this.showBlockModal = false;
  }

  deleteUser() {
    console.log('Delete user:', this.user?.id);
    this.showDeleteModal = false;
    this.router.navigate(['/admin/users']);
  }
}
