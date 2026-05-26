import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AnalyticsService, CustomerAnalyticsResponse, ProductPurchaseHistory } from '../../services/analytics.service';
import { AnalyticsPeriod } from '../../models/api.models';
import { AuthService } from '../../services/auth.service';

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
  reportError: string = '';
  isGeneratingSupplierSummary: boolean = false;
  isGeneratingProductHistory: boolean = false;
  selectedProductId: number | null = null;

  private expensesChartInstance?: ChartInstance;
  private suppliersChartInstance?: ChartInstance;
  private chartModulePromise?: Promise<ChartModule>;

  constructor(
    private analyticsService: AnalyticsService,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  get selectedProductHistory(): ProductPurchaseHistory | null {
    if (this.selectedProductId == null) {
      return null;
    }
    return this.analytics?.productHistory.find(p => p.productId === this.selectedProductId) || null;
  }

  private get reportPeriodRange(): { from: string; to: string } {
    const to = new Date();
    const from = new Date(to);
    switch (this.selectedPeriod) {
      case 'week': from.setDate(to.getDate() - 7); break;
      case 'quarter': from.setMonth(to.getMonth() - 3); break;
      case 'year': from.setFullYear(to.getFullYear() - 1); break;
      case 'month':
      default: from.setMonth(to.getMonth() - 1); break;
    }
    return { from: from.toLocaleDateString('ru-RU'), to: to.toLocaleDateString('ru-RU') };
  }

  downloadSupplierSummary(): void {
    if (!this.analytics) {
      return;
    }
    const companyId = this.authService.getCompanyId();
    if (!companyId) {
      this.reportError = 'Не удалось определить текущую компанию.';
      return;
    }

    this.reportError = '';
    this.isGeneratingSupplierSummary = true;

    this.authService.getCompanyProfile(companyId).subscribe({
      next: profile => {
        const rows = (this.analytics?.supplierAnalytics || []).map(item => ({
          supplierName: item.supplierName,
          orderCount: item.orderCount,
          totalAmount: item.totalAmount,
          averageCheck: item.averageCheck,
          lastOrderDate: this.formatDate(item.lastOrderDate)
        }));
        const totalAmount = rows.reduce((sum, r) => sum + Number(r.totalAmount || 0), 0);
        const totalOrders = rows.reduce((sum, r) => sum + Number(r.orderCount || 0), 0);
        const overallAverageCheck = totalOrders > 0 ? totalAmount / totalOrders : 0;

        const range = this.reportPeriodRange;
        const payload = {
          customerName: profile.legalName || profile.name || 'Торговая сеть',
          periodFrom: range.from,
          periodTo: range.to,
          rows,
          totalAmount,
          overallAverageCheck
        };

        this.http.post('/api/documents/reports/supplier-summary', payload, { responseType: 'blob' }).subscribe({
          next: blob => {
            this.savePdf(blob, 'supplier-summary-report.pdf');
            this.isGeneratingSupplierSummary = false;
          },
          error: error => this.handleReportError(error, 'isGeneratingSupplierSummary')
        });
      },
      error: error => this.handleReportError(error, 'isGeneratingSupplierSummary')
    });
  }

  downloadProductHistory(): void {
    const history = this.selectedProductHistory;
    if (!history) {
      this.reportError = 'Выберите товар для формирования отчёта.';
      return;
    }

    this.reportError = '';
    this.isGeneratingProductHistory = true;

    const rows = history.purchases.map(p => ({
      date: this.formatDate(p.date),
      supplierName: p.supplierName,
      quantity: p.quantity,
      unitPrice: p.unitPrice,
      totalPrice: p.totalPrice
    }));
    const prices = history.purchases.map(p => Number(p.unitPrice || 0)).filter(v => v > 0);
    const minPrice = prices.length ? Math.min(...prices) : 0;
    const maxPrice = prices.length ? Math.max(...prices) : 0;

    const range = this.reportPeriodRange;
    const payload = {
      productName: history.productName,
      productSku: history.productSku,
      periodFrom: range.from,
      periodTo: range.to,
      rows,
      minPrice,
      maxPrice
    };

    this.http.post('/api/documents/reports/product-purchase-history', payload, { responseType: 'blob' }).subscribe({
      next: blob => {
        this.savePdf(blob, 'product-purchase-history-report.pdf');
        this.isGeneratingProductHistory = false;
      },
      error: error => this.handleReportError(error, 'isGeneratingProductHistory')
    });
  }

  private savePdf(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = window.document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }

  private handleReportError(error: unknown, busyFlag: 'isGeneratingSupplierSummary' | 'isGeneratingProductHistory'): void {
    console.error('Failed to download report:', error);
    this.reportError = 'Не удалось сформировать отчёт.';
    this[busyFlag] = false;
  }

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

