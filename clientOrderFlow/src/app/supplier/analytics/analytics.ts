import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService, SupplierAnalyticsResponse } from '../../services/analytics.service';
import { AnalyticsPeriod } from '../../models/api.models';

type ChartModule = typeof import('chart.js/auto');
type ChartInstance = { destroy(): void };

@Component({
  selector: 'app-analytics',
  imports: [CommonModule],
  templateUrl: './analytics.html',
  styleUrl: './analytics.css'
})
export class Analytics implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('salesChart') salesChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('topProductsChart') topProductsChart?: ElementRef<HTMLCanvasElement>;

  selectedPeriod: AnalyticsPeriod = 'month';
  periods: { value: AnalyticsPeriod; label: string }[] = [
    { value: 'week', label: 'Неделя' },
    { value: 'month', label: 'Месяц' },
    { value: 'quarter', label: 'Квартал' },
    { value: 'year', label: 'Год' }
  ];

  analytics: SupplierAnalyticsResponse | null = null;
  isLoading: boolean = false;

  private salesChartInstance?: ChartInstance;
  private topProductsChartInstance?: ChartInstance;
  private chartModulePromise?: Promise<ChartModule>;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit() {
    this.loadAnalytics();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      void this.initCharts();
    }, 100);
  }

  ngOnDestroy() {
    this.salesChartInstance?.destroy();
    this.topProductsChartInstance?.destroy();
  }

  loadAnalytics() {
    this.isLoading = true;
    this.analyticsService.getSupplierAnalytics(this.selectedPeriod).subscribe({
      next: (data) => {
        this.analytics = data;
        this.isLoading = false;
        setTimeout(() => {
          void this.initCharts();
        }, 100);
      },
      error: (error) => {
        console.error('Error loading analytics:', error);
        this.isLoading = false;
      }
    });
  }

  onPeriodChange(period: AnalyticsPeriod) {
    this.selectedPeriod = period;
    this.loadAnalytics();
  }

  private async initCharts(): Promise<void> {
    if (!this.analytics) return;
    await this.initSalesChart();
    await this.initTopProductsChart();
  }

  private async initSalesChart(): Promise<void> {
    if (!this.salesChart?.nativeElement || !this.analytics) return;

    this.salesChartInstance?.destroy();

    const ctx = this.salesChart.nativeElement.getContext('2d');
    if (ctx) {
      const { default: Chart } = await this.loadChartModule();
      const labels = this.analytics.salesDynamics.map(d => {
        const date = new Date(d.date);
        return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' });
      });
      const data = this.analytics.salesDynamics.map(d => d.revenue);

      this.salesChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels,
          datasets: [{
            label: 'Выручка (BYN)',
            data,
            borderColor: 'rgba(79, 70, 229, 1)',
            backgroundColor: 'rgba(79, 70, 229, 0.1)',
            tension: 0.4,
            fill: true
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: { y: { beginAtZero: true } }
        }
      });
    }
  }

  private async initTopProductsChart(): Promise<void> {
    if (!this.topProductsChart?.nativeElement || !this.analytics) return;

    this.topProductsChartInstance?.destroy();

    const ctx = this.topProductsChart.nativeElement.getContext('2d');
    if (ctx) {
      const { default: Chart } = await this.loadChartModule();
      const topProducts = this.analytics.productAnalytics.topByRevenue.slice(0, 5);
      const labels = topProducts.map(p => p.productName.substring(0, 15) + (p.productName.length > 15 ? '...' : ''));
      const data = topProducts.map(p => p.revenue);

      this.topProductsChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: 'Выручка (BYN)',
            data,
            backgroundColor: [
              'rgba(79, 70, 229, 0.7)',
              'rgba(59, 130, 246, 0.7)',
              'rgba(16, 185, 129, 0.7)',
              'rgba(245, 158, 11, 0.7)',
              'rgba(239, 68, 68, 0.7)'
            ],
            borderRadius: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: { y: { beginAtZero: true } }
        }
      });
    }
  }

  private loadChartModule(): Promise<ChartModule> {
    if (!this.chartModulePromise) {
      this.chartModulePromise = import('chart.js/auto');
    }

    return this.chartModulePromise;
  }

  formatCurrency(value: number): string {
    return value.toLocaleString('ru-RU', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }
}
