import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { Header } from '../shared/header/header';
import { AdminService, RegistrationActivityPoint } from '../../services/admin.service';

type ChartModule = typeof import('chart.js/auto');
type ChartInstance = { destroy(): void };

interface TopEntity {
  name: string;
  meta: string;
}

@Component({
  selector: 'admin-analytics',
  standalone: true,
  imports: [CommonModule, Header],
  templateUrl: './analytics.html',
  styleUrls: ['./analytics.css']
})
export class Analytics implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mainChart') mainChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('rolesChart') rolesChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('usersChart') usersChart?: ElementRef<HTMLCanvasElement>;

  selectedPeriod: string = 'month';
  isLoading: boolean = false;
  loadError: string | null = null;

  kpiData = {
    totalUsers: '0',
    totalOrders: '0',
    totalRevenue: '0',
    averageCheck: '0'
  };

  recentSupportItems: TopEntity[] = [];
  recentRegistrationItems: TopEntity[] = [];
  roleBreakdown = {
    suppliers: 0,
    retailers: 0
  };
  registrationActivity: Array<{ date: string; count: number }> = [];

  private mainChartInstance?: ChartInstance;
  private rolesChartInstance?: ChartInstance;
  private usersChartInstance?: ChartInstance;
  private chartModulePromise?: Promise<ChartModule>;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadAnalytics();
  }

  ngAfterViewInit() {
    if (!this.isLoading) {
      void this.initCharts();
    }
  }

  setPeriod(period: string) {
    this.selectedPeriod = period;
    this.loadAnalytics();
  }

  private loadAnalytics(): void {
    this.isLoading = true;
    this.loadError = null;

    forkJoin({
      dashboard: this.adminService.getDashboardStats(),
      users: this.adminService.getUsersStats(),
      orders: this.adminService.getOrdersStats(),
      registrations: this.adminService.getRecentRegistrations(),
      supportTickets: this.adminService.getRecentSupportTickets(),
      activity: this.adminService.getRegistrationActivity(this.selectedPeriod === 'today' ? 1 : this.selectedPeriod === 'week' ? 7 : this.selectedPeriod === 'quarter' ? 90 : 30)
    }).subscribe({
      next: ({ dashboard, users, orders, registrations, supportTickets, activity }) => {
        this.kpiData = {
          totalUsers: Number(dashboard.totalUsers || 0).toLocaleString('ru-RU'),
          totalOrders: Number(orders['totalOrdersThisMonth'] || 0).toLocaleString('ru-RU'),
          totalRevenue: Number(orders['totalRevenue'] || 0).toLocaleString('ru-RU', { maximumFractionDigits: 0 }),
          averageCheck: Number((Number(orders['totalRevenue'] || 0) / Math.max(Number(orders['totalOrdersThisMonth'] || 1), 1)) || 0).toLocaleString('ru-RU', { maximumFractionDigits: 2 })
        };

        this.roleBreakdown = {
          suppliers: Number(users.suppliers || 0),
          retailers: Number(users.retailers || 0)
        };

        this.registrationActivity = activity.map((point: RegistrationActivityPoint) => ({
          date: point.date,
          count: Number(point.count || 0)
        }));

        this.recentSupportItems = supportTickets.slice(0, 5).map(item => ({
          name: item.userEmail || item.subject || `Тикет #${item.ticketId || item.id}`,
          meta: String(item.status || 'OPEN')
        }));

        this.recentRegistrationItems = registrations.slice(0, 5).map(item => ({
          name: item.companyName,
          meta: item.role
        }));

        this.isLoading = false;
        setTimeout(() => {
          void this.initCharts();
        });
      },
      error: error => {
        console.error('Error loading admin analytics:', error);
        this.loadError = 'Не удалось загрузить аналитику.';
        this.isLoading = false;
      }
    });
  }

  private async initCharts(): Promise<void> {
    await this.initMainChart();
    await this.initRolesChart();
    await this.initUsersChart();
  }

  private async initMainChart(): Promise<void> {
    const ctx = this.mainChart?.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    this.mainChartInstance?.destroy();
    this.mainChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: this.registrationActivity.map(point => new Date(point.date).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' })),
        datasets: [{
          label: 'Новые регистрации',
          data: this.registrationActivity.map(point => point.count),
          borderColor: 'rgba(79, 70, 229, 1)',
          backgroundColor: 'rgba(79, 70, 229, 0.12)',
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    });
  }

  private async initRolesChart(): Promise<void> {
    const ctx = this.rolesChart?.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    this.rolesChartInstance?.destroy();
    this.rolesChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Поставщики', 'Торговые сети'],
        datasets: [{
          data: [this.roleBreakdown.suppliers, this.roleBreakdown.retailers],
          backgroundColor: [
            'rgba(34, 197, 94, 0.8)',
            'rgba(59, 130, 246, 0.8)'
          ],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom'
          }
        }
      }
    });
  }

  private async initUsersChart(): Promise<void> {
    const ctx = this.usersChart?.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    this.usersChartInstance?.destroy();
    this.usersChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: this.registrationActivity.map(point => new Date(point.date).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' })),
        datasets: [{
          label: 'Новые регистрации',
          data: this.registrationActivity.map(point => point.count),
          backgroundColor: 'rgba(79, 70, 229, 0.8)',
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    });
  }

  private loadChartModule(): Promise<ChartModule> {
    if (!this.chartModulePromise) {
      this.chartModulePromise = import('chart.js/auto');
    }

    return this.chartModulePromise;
  }

  ngOnDestroy(): void {
    this.mainChartInstance?.destroy();
    this.rolesChartInstance?.destroy();
    this.usersChartInstance?.destroy();
  }
}
