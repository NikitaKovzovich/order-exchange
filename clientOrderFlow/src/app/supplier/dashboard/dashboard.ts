import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AnalyticsService } from '../../services/analytics.service';
import { CatalogService } from '../../services/catalog.service';
import { Inventory, Order } from '../../models/api.models';

type ChartModule = typeof import('chart.js/auto');
type ChartInstance = { destroy(): void };

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, AfterViewInit {
  @ViewChild('salesChart') salesChart?: ElementRef<HTMLCanvasElement>;

  stats = {
    revenue: '0',
    newOrders: 3,
    avgCheck: '0',
    inTransit: 0
  };

  pendingOrders: Array<{ id: string; retailer: string; amount: string; date: string }> = [];

  lowStockProducts: Array<{ name: string; stock: number; unit: string }> = [];
  salesDynamics: Array<{ date: string; revenue: number }> = [];

  private chart?: ChartInstance;
  private chartModulePromise?: Promise<ChartModule>;

  constructor(
    private analyticsService: AnalyticsService,
    private catalogService: CatalogService
  ) {}

  ngOnInit() {
    this.loadDashboard();
    this.loadLowStockProducts();
  }

  ngAfterViewInit() {
    if (this.salesChart) {
      void this.initChart();
    }
  }

  private async initChart(): Promise<void> {
    const ctx = this.salesChart?.nativeElement.getContext('2d');
    if (!ctx) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    this.chart?.destroy();
    this.chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: this.salesDynamics.map(item => new Date(item.date).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' })),
        datasets: [{
          label: 'Выручка (BYN)',
          data: this.salesDynamics.map(item => item.revenue),
          backgroundColor: 'rgba(79, 70, 229, 0.7)',
          borderColor: 'rgba(79, 70, 229, 1)',
          borderWidth: 1,
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
        },
        plugins: {
          legend: {
            display: false
          }
        }
      }
    });
  }

  private loadDashboard(): void {
    this.analyticsService.getSupplierDashboard().subscribe({
      next: dashboard => {
        this.stats = {
          revenue: Number(dashboard.revenueThisMonth || 0).toLocaleString('ru-RU'),
          newOrders: dashboard.newOrdersToday,
          avgCheck: Number(dashboard.averageCheck || 0).toLocaleString('ru-RU'),
          inTransit: dashboard.ordersInTransit
        };

        this.pendingOrders = (dashboard.pendingConfirmationOrders || []).map((order: Order) => ({
          id: order.id.toString(),
          retailer: order.customerName || `Компания #${order.customerId}`,
          amount: Number(order.totalAmount || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
          date: new Date(order.createdAt).toLocaleDateString('ru-RU')
        }));

        this.salesDynamics = (dashboard.salesDynamics7Days || []).map((item: { date: string; revenue: number }) => ({
          date: item.date,
          revenue: Number(item.revenue || 0)
        }));

        if (this.salesChart) {
          void this.initChart();
        }
      },
      error: error => console.error('Error loading supplier dashboard:', error)
    });
  }

  private loadLowStockProducts(): void {
    this.catalogService.getLowStockProducts().subscribe({
      next: (items: Inventory[]) => {
        this.lowStockProducts = items.map(item => ({
          name: item.productName,
          stock: item.quantityAvailable,
          unit: 'шт.'
        }));
      },
      error: error => console.error('Error loading low stock products:', error)
    });
  }

  private loadChartModule(): Promise<ChartModule> {
    if (!this.chartModulePromise) {
      this.chartModulePromise = import('chart.js/auto');
    }

    return this.chartModulePromise;
  }
}
