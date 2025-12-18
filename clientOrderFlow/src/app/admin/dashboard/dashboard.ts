import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Chart } from 'chart.js';
import { Header } from '../shared/header/header';
import { AdminService, PendingVerificationRequest } from '../../services/admin.service';
import { ChatService } from '../../services/chat.service';
import { SupportTicket } from '../../models/api.models';

interface StatCard {
  title: string;
  value: string | number;
  change: string;
  icon: string;
  trend: 'up' | 'down';
  colorClass: string;
}

@Component({
  selector: 'admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, Header],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class Dashboard implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('usersChart') usersChart?: ElementRef<HTMLCanvasElement>;

  stats: StatCard[] = [];
  registrationRequests: PendingVerificationRequest[] = [];
  supportTickets: SupportTicket[] = [];
  isLoading: boolean = false;

  private chartInstance?: Chart;

  constructor(
    private adminService: AdminService,
    private chatService: ChatService
  ) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  ngAfterViewInit() {
    setTimeout(() => this.initUsersChart(), 100);
  }

  ngOnDestroy() {
    this.chartInstance?.destroy();
  }

  loadDashboardData() {
    this.isLoading = true;

    this.adminService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = [
          {
            title: 'Всего пользователей',
            value: data.totalUsers.toLocaleString('ru-RU'),
            change: '',
            icon: 'users',
            trend: 'up',
            colorClass: 'bg-blue-100 text-blue-600'
          },
          {
            title: 'Активных заказов',
            value: data.activeOrders.toLocaleString('ru-RU'),
            change: '',
            icon: 'orders',
            trend: 'up',
            colorClass: 'bg-green-100 text-green-600'
          },
          {
            title: 'Ожидают верификации',
            value: data.pendingVerifications,
            change: '',
            icon: 'verification',
            trend: data.pendingVerifications > 0 ? 'up' : 'down',
            colorClass: 'bg-yellow-100 text-yellow-600'
          }
        ];
      },
      error: (error) => console.error('Error loading stats:', error)
    });

    this.adminService.getPendingVerificationRequests().subscribe({
      next: (requests) => {
        this.registrationRequests = requests.slice(0, 5);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading verification requests:', error);
        this.isLoading = false;
      }
    });

    this.chatService.getAllTickets().subscribe({
      next: (tickets) => {
        this.supportTickets = tickets.filter(t => t.status !== 'CLOSED').slice(0, 5);
      },
      error: (error) => console.error('Error loading tickets:', error)
    });
  }

  private initUsersChart() {
    const ctx = this.usersChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['Неделя 1', 'Неделя 2', 'Неделя 3', 'Неделя 4'],
          datasets: [{
            label: 'Новые пользователи',
            data: [5, 12, 8, 25],
            backgroundColor: 'rgba(79, 70, 229, 0.1)',
            borderColor: 'rgba(79, 70, 229, 1)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { y: { beginAtZero: true } },
          plugins: { legend: { display: false } }
        }
      });
    }
  }

  getRoleClass(role: string): string {
    return role === 'SUPPLIER' ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800';
  }

  getRoleLabel(role: string): string {
    return role === 'SUPPLIER' ? 'Поставщик' : 'Торговая сеть';
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'OPEN': 'bg-red-100 text-red-800',
      'IN_PROGRESS': 'bg-yellow-100 text-yellow-800',
      'RESOLVED': 'bg-gray-100 text-gray-800'
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ru-RU');
  }
}
