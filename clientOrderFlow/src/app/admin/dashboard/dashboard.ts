import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Header } from '../shared/header/header';
import { AdminService, RecentRegistrationItem, RecentSupportTicketItem, RegistrationActivityPoint } from '../../services/admin.service';

type ChartModule = typeof import('chart.js/auto');
type ChartInstance = { destroy(): void };

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
  registrationRequests: RecentRegistrationItem[] = [];
  supportTickets: RecentSupportTicketItem[] = [];
  isLoading: boolean = false;
  registrationActivity: Array<{ date: string; count: number }> = [];

  private chartInstance?: ChartInstance;
  private chartModulePromise?: Promise<ChartModule>;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      void this.initUsersChart();
    }, 100);
  }

  ngOnDestroy() {
    this.chartInstance?.destroy();
  }

  loadDashboardData() {
    this.isLoading = true;

    forkJoin({
      dashboard: this.adminService.getDashboardStats(),
      ordersStats: this.adminService.getOrdersStats(),
      verificationRate: this.adminService.getVerificationRate(),
      recentRegistrations: this.adminService.getRecentRegistrations(),
      recentSupportTickets: this.adminService.getRecentSupportTickets(),
      registrationActivity: this.adminService.getRegistrationActivity()
    }).subscribe({
      next: ({ dashboard, ordersStats, verificationRate, recentRegistrations, recentSupportTickets, registrationActivity }) => {
        this.stats = [
          {
            title: 'Всего пользователей',
            value: dashboard.totalUsers.toLocaleString('ru-RU'),
            change: `+${dashboard.usersWeekGrowth || 0} за неделю`,
            icon: 'users',
            trend: 'up',
            colorClass: 'bg-blue-100 text-blue-600'
          },
          {
            title: 'Заказов за период',
            value: Number(ordersStats['totalOrdersThisMonth'] ?? 0).toLocaleString('ru-RU'),
            change: `${ordersStats['orderGrowthPercent'] ?? 0}%`,
            icon: 'orders',
            trend: Number(ordersStats['orderGrowthPercent'] ?? 0) >= 0 ? 'up' : 'down',
            colorClass: 'bg-green-100 text-green-600'
          },
          {
            title: 'Ожидают верификации',
            value: dashboard.pendingVerifications,
            change: '',
            icon: 'verification',
            trend: dashboard.pendingVerifications > 0 ? 'up' : 'down',
            colorClass: 'bg-yellow-100 text-yellow-600'
          },
          {
            title: 'Новые тикеты',
            value: recentSupportTickets.length,
            change: 'последние обращения',
            icon: 'support',
            trend: recentSupportTickets.length > 0 ? 'up' : 'down',
            colorClass: 'bg-red-100 text-red-600'
          },
          {
            title: 'Выручка за период',
            value: `${Number(ordersStats['totalRevenue'] ?? 0).toLocaleString('ru-RU')} BYN`,
            change: `${ordersStats['revenueGrowthPercent'] ?? 0}%`,
            icon: 'revenue',
            trend: Number(ordersStats['revenueGrowthPercent'] ?? 0) >= 0 ? 'up' : 'down',
            colorClass: 'bg-green-100 text-green-600'
          },
          {
            title: 'Approval rate',
            value: `${verificationRate['approvalRate'] ?? 0}%`,
            change: `approved: ${verificationRate['approved'] ?? 0}`,
            icon: 'verification',
            trend: Number(verificationRate['approvalRate'] ?? 0) >= 50 ? 'up' : 'down',
            colorClass: 'bg-indigo-100 text-indigo-600'
          }
        ];
        this.registrationRequests = recentRegistrations.slice(0, 5);
        this.supportTickets = recentSupportTickets.slice(0, 5);
        this.registrationActivity = registrationActivity.map((point: RegistrationActivityPoint) => ({
          date: point.date,
          count: Number(point.count)
        }));
        void this.initUsersChart();
        this.isLoading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading dashboard data:', error);
        this.isLoading = false;
      }
    });
  }

  private async initUsersChart(): Promise<void> {
    const ctx = this.usersChart?.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    this.chartInstance?.destroy();
    this.chartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: this.registrationActivity.map(point => new Date(point.date).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' })),
        datasets: [{
          label: 'Новые пользователи',
          data: this.registrationActivity.map(point => point.count),
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

  private loadChartModule(): Promise<ChartModule> {
    if (!this.chartModulePromise) {
      this.chartModulePromise = import('chart.js/auto');
    }

    return this.chartModulePromise;
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
      'RESOLVED': 'bg-gray-100 text-gray-800',
      'CLOSED': 'bg-gray-100 text-gray-800'
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ru-RU');
  }
}
