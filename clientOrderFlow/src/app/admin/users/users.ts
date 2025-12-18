import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Header } from '../shared/header/header';
import { AdminService, User } from '../../services/admin.service';

@Component({
  selector: 'admin-users',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, Header],
  templateUrl: './users.html',
  styleUrls: ['./users.css']
})
export class Users implements OnInit {
  activeTab: 'providers' | 'retailers' = 'providers';
  searchTerm: string = '';
  selectedStatus: string = '';
  isLoading: boolean = false;

  users: User[] = [];
  filteredProviders: User[] = [];
  filteredRetailers: User[] = [];

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading = true;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.filterUsers();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.isLoading = false;
      }
    });
  }

  setActiveTab(tab: 'providers' | 'retailers') {
    this.activeTab = tab;
    this.filterUsers();
  }

  filterUsers() {
    const providers = this.users.filter(u => u.role === 'SUPPLIER');
    const retailers = this.users.filter(u => u.role === 'RETAIL_CHAIN');

    this.filteredProviders = providers.filter(user => {
      const matchesSearch = !this.searchTerm ||
        user.company?.legalName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.selectedStatus || user.status === this.selectedStatus;
      return matchesSearch && matchesStatus;
    });

    this.filteredRetailers = retailers.filter(user => {
      const matchesSearch = !this.searchTerm ||
        user.company?.legalName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.selectedStatus || user.status === this.selectedStatus;
      return matchesSearch && matchesStatus;
    });
  }

  blockUser(userId: number) {
    if (!confirm('Заблокировать пользователя?')) return;

    this.adminService.blockUser(userId).subscribe({
      next: () => this.loadUsers(),
      error: (error) => console.error('Error blocking user:', error)
    });
  }

  unblockUser(userId: number) {
    this.adminService.unblockUser(userId).subscribe({
      next: () => this.loadUsers(),
      error: (error) => console.error('Error unblocking user:', error)
    });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ru-RU');
  }
}
