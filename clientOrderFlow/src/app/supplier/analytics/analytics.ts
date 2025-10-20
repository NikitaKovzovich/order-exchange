import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart } from 'chart.js';

@Component({
  selector: 'app-analytics',
  imports: [CommonModule],
  templateUrl: './analytics.html',
  styleUrl: './analytics.css'
})
export class Analytics implements OnInit, AfterViewInit {
  @ViewChild('salesChart') salesChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('topProductsChart') topProductsChart?: ElementRef<HTMLCanvasElement>;

  selectedPeriod: string = 'Неделя';
  periods = ['День', 'Неделя', 'Месяц', 'Квартал', 'Год'];

  stats = {
    totalRevenue: '408 000',
    totalOrders: 33,
    avgCheck: '12 363',
    totalShipped: 1450
  };

  funnelData = [
    { stage: 'Ожидает подтвержд.', count: 45, percentage: 100, color: 'bg-blue-500' },
    { stage: 'Подтвержден', count: 43, percentage: 96, color: 'bg-blue-400' },
    { stage: 'Ожидает оплаты', count: 42, percentage: 93, color: 'bg-indigo-400' },
    { stage: 'Оплачен', count: 37, percentage: 82, color: 'bg-indigo-500' },
    { stage: 'Ожидает отгрузки', count: 35, percentage: 78, color: 'bg-purple-500' },
    { stage: 'В пути', count: 33, percentage: 73, color: 'bg-purple-600' },
    { stage: 'Доставлен', count: 31, percentage: 69, color: 'bg-green-500' },
    { stage: 'Закрыт', count: 30, percentage: 67, color: 'bg-green-600' }
  ];

  topProductsByRevenue = [
    { name: 'Сыр "Российский" весовой', revenue: '88 200' },
    { name: 'Молоко "Деревенское" 3.2% 1л', revenue: '65 500' },
    { name: 'Масло сливочное 82.5%', revenue: '45 100' },
    { name: 'Хлеб "Бородинский" (нарезка)', revenue: '32 400' },
    { name: 'Творог 9%', revenue: '28 900' }
  ];

  topProductsByQuantity = [
    { name: 'Молоко "Деревенское" 3.2% 1л', quantity: 850 },
    { name: 'Хлеб "Бородинский" (нарезка)', quantity: 720 },
    { name: 'Йогурт питьевой "Клубника"', quantity: 540 },
    { name: 'Батон "Нарезной"', quantity: 490 },
    { name: 'Сметана 20%', quantity: 350 }
  ];

  clientAnalytics = [
    { name: 'Сеть Магазинов', orders: 12, revenue: '180 500', avgCheck: '15 041', lastOrder: '15.10.2025' },
    { name: 'Гипермаркет "Центр"', orders: 8, revenue: '120 200', avgCheck: '15 025', lastOrder: '14.10.2025' },
    { name: 'Супермаркет "Угол"', orders: 13, revenue: '107 300', avgCheck: '8 253', lastOrder: '12.10.2025' }
  ];

  private salesChartInstance?: Chart;
  private topProductsChartInstance?: Chart;

  ngOnInit() {
  }

  ngAfterViewInit() {
    this.initSalesChart();
    this.initTopProductsChart();
  }

  private initSalesChart() {
    const ctx = this.salesChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.salesChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'],
          datasets: [{
            label: 'Выручка (BYN)',
            data: [12000, 19000, 15000, 18000, 22000, 31000, 25000],
            borderColor: 'rgba(79, 70, 229, 1)',
            backgroundColor: 'rgba(79, 70, 229, 0.1)',
            tension: 0.4,
            fill: true
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            }
          },
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    }
  }

  private initTopProductsChart() {
    const ctx = this.topProductsChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.topProductsChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['Молоко', 'Хлеб', 'Сыр', 'Масло', 'Творог'],
          datasets: [{
            label: 'Выручка (BYN)',
            data: [1125, 684, 2250, 2625, 912],
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
          plugins: {
            legend: {
              display: false
            }
          },
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    }
  }
}
