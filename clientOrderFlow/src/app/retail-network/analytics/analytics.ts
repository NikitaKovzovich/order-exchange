import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnalyticsService, CustomerAnalyticsResponse } from '../../services/analytics.service';
import { AnalyticsPeriod } from '../../models/api.models';

type ChartModule = typeof import('chart.js/auto');
type ChartInstance = { destroy(): void };

@Component({
  selector: 'app-retail-analytics',
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics.html',
  styleUrl: './analytics.css'
})
export class Analytics implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('expensesChart') expensesChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('suppliersChart') suppliersChart?: ElementRef<HTMLCanvasElement>;

  selectedPeriod: AnalyticsPeriod = 'month';
  periods: { value: AnalyticsPeriod; label: string }[] = [
    { value: 'week', label: 'Неделя' },
    { value: 'month', label: 'Месяц' },
    { value: 'quarter', label: 'Квартал' },
    { value: 'year', label: 'Год' }
  ];

  analytics: CustomerAnalyticsResponse | null = null;
  isLoading: boolean = false;
  errorMessage: string = '';

  private expensesChartInstance?: ChartInstance;
  private suppliersChartInstance?: ChartInstance;
  private chartModulePromise?: Promise<ChartModule>;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadAnalytics();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      void this.initCharts();
    }, 100);
  }

  ngOnDestroy(): void {
    this.expensesChartInstance?.destroy();
    this.suppliersChartInstance?.destroy();
  }

  onPeriodChange(period: AnalyticsPeriod): void {
    this.selectedPeriod = period;
    this.loadAnalytics();
  }

  formatCurrency(value: number): string {
    return Number(value || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatDate(value?: string): string {
    return value ? new Date(value).toLocaleDateString('ru-RU') : '—';
  }

  get topSuppliers() {
    return (this.analytics?.supplierAnalytics || []).slice(0, 5);
  }

  private loadAnalytics(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.analyticsService.getCustomerAnalytics(this.selectedPeriod).subscribe({
      next: analytics => {
        this.analytics = analytics;
        this.isLoading = false;
        setTimeout(() => {
          void this.initCharts();
        }, 100);
      },
      error: error => {
        console.error('Error loading retail analytics:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить аналитику торговой сети.';
        this.isLoading = false;
      }
    });
  }

  private async initCharts(): Promise<void> {
    if (!this.analytics) {
      return;
    }

    await this.initExpensesChart();
    await this.initSuppliersChart();
  }

  private async initExpensesChart(): Promise<void> {
    const ctx = this.expensesChart?.nativeElement.getContext('2d');
    if (!ctx || !this.analytics) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    this.expensesChartInstance?.destroy();
    this.expensesChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: this.analytics.expensesDynamics.map(item => new Date(item.date).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' })),
        datasets: [{
          label: 'Расходы (BYN)',
          data: this.analytics.expensesDynamics.map(item => item.expenses),
          borderColor: 'rgba(79, 70, 229, 1)',
          backgroundColor: 'rgba(79, 70, 229, 0.10)',
          tension: 0.35,
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

  private async initSuppliersChart(): Promise<void> {
    const ctx = this.suppliersChart?.nativeElement.getContext('2d');
    if (!ctx || !this.analytics) {
      return;
    }

    const { default: Chart } = await this.loadChartModule();

    const supplierData = this.analytics.expenseStructure.bySupplier.slice(0, 5);
    this.suppliersChartInstance?.destroy();
    this.suppliersChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: supplierData.map(item => item.supplierName || `Поставщик #${item.supplierId}`),
        datasets: [{
          data: supplierData.map(item => item.amount),
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

  private loadChartModule(): Promise<ChartModule> {
    if (!this.chartModulePromise) {
      this.chartModulePromise = import('chart.js/auto');
    }

    return this.chartModulePromise;
  }
}

