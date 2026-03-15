import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AnalyticsService } from '../../services/analytics.service';
import { Order } from '../../models/api.models';

type ChartModule = typeof import('chart.js/auto');
type ChartInstance = { destroy(): void };

@Component({
  selector: 'app-retail-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, AfterViewInit {
  @ViewChild('expensesDynamicsChart') expensesDynamicsChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('expensesChart') expensesChart?: ElementRef<HTMLCanvasElement>;

  stats = {
    totalExpenses: '0',
    totalOrders: 0,
    activeSuppliers: 0,
    avgOrderAmount: '0'
  };

  recentOrders: Array<{ id: string; supplier: string; amount: string; status: string; statusLabel: string }> = [];
  expensesDynamics: Array<{ date: string; expenses: number }> = [];
  expenseStructure: Array<{ label: string; value: number }> = [];

  private dynamicsChart?: ChartInstance;
  private structureChart?: ChartInstance;
  private chartModulePromise?: Promise<ChartModule>;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit() {
    this.loadDashboard();
  }

  ngAfterViewInit() {
    void this.initDynamicsChart();
    void this.initStructureChart();
  }

  private async initDynamicsChart(): Promise<void> {
    const ctx = this.expensesDynamicsChart?.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    this.dynamicsChart?.destroy();
    this.dynamicsChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: this.expensesDynamics.map(item => new Date(item.date).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' })),
        datasets: [{
          label: 'Расходы (BYN)',
          data: this.expensesDynamics.map(item => item.expenses),
          backgroundColor: 'rgba(79, 70, 229, 0.7)',
          borderColor: 'rgba(79, 70, 229, 1)',
          borderWidth: 1,
          borderRadius: 4
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

  private async initStructureChart(): Promise<void> {
    const ctx = this.expensesChart?.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    this.structureChart?.destroy();
    this.structureChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: this.expenseStructure.map(item => item.label),
        datasets: [{
          data: this.expenseStructure.map(item => item.value),
          backgroundColor: ['#4F46E5', '#6366F1', '#818CF8', '#A5B4FC', '#C7D2FE'],
          hoverOffset: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false
      }
    });
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'AWAITING_PAYMENT': 'text-yellow-800 bg-yellow-200',
      'SHIPPED': 'text-blue-600 bg-blue-200',
      'DELIVERED': 'text-green-800 bg-green-200'
    };
    return classes[status] || 'text-gray-600 bg-gray-200';
  }

  private loadDashboard(): void {
    this.analyticsService.getCustomerDashboard().subscribe({
      next: dashboard => {
        this.stats = {
          totalExpenses: Number(dashboard.expensesThisMonth || 0).toLocaleString('ru-RU'),
          totalOrders: dashboard.orderCount,
          activeSuppliers: dashboard.activeContractsCount,
          avgOrderAmount: Number(dashboard.averageCheck || 0).toLocaleString('ru-RU')
        };

        this.recentOrders = (dashboard.recentOrders || []).map((order: Order) => ({
          id: order.id.toString(),
          supplier: order.supplierName || `Поставщик #${order.supplierId}`,
          amount: Number(order.totalAmount || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
          status: order.statusCode,
          statusLabel: order.statusName
        }));

        this.expenseStructure = (dashboard.expenseStructure?.byCategory || []).map((item: { categoryName?: string; amount: number }) => ({
          label: item.categoryName || 'Без категории',
          value: Number(item.amount || 0)
        }));

        this.expensesDynamics = (dashboard.expensesDynamics7Days || []).map((item: { date: string; expenses: number }) => ({
          date: item.date,
          expenses: Number(item.expenses || 0)
        }));

        void this.initDynamicsChart();
        void this.initStructureChart();
      },
      error: error => console.error('Error loading customer dashboard:', error)
    });
  }

  private loadChartModule(): Promise<ChartModule> {
    if (!this.chartModulePromise) {
      this.chartModulePromise = import('chart.js/auto');
    }

    return this.chartModulePromise;
  }
}
