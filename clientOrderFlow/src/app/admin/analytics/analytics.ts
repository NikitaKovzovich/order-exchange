import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart } from 'chart.js';
import { Header } from '../shared/header/header';

interface TopEntity {
  name: string;
  revenue: string;
}

@Component({
  selector: 'admin-analytics',
  standalone: true,
  imports: [CommonModule, Header],
  templateUrl: './analytics.html',
  styleUrls: ['./analytics.css']
})
export class Analytics implements OnInit, AfterViewInit {
  @ViewChild('mainChart') mainChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('rolesChart') rolesChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('usersChart') usersChart?: ElementRef<HTMLCanvasElement>;

  selectedPeriod: string = 'month';

  kpiData = {
    totalUsers: '1,254',
    totalOrders: '5,890',
    totalRevenue: '1.2M',
    averageCheck: '203.73'
  };

  topSuppliers: TopEntity[] = [
    { name: 'Продукты Оптом', revenue: '125,340' },
    { name: 'Молочный Мир', revenue: '98,120' },
    { name: 'ФруктТорг', revenue: '76,500' },
    { name: 'ХлебПром', revenue: '54,900' },
    { name: 'Бакалея-Сервис', revenue: '41,200' }
  ];

  topRetailers: TopEntity[] = [
    { name: 'Сеть Магазинов', revenue: '210,450' },
    { name: 'Гипермаркет "Центр"', revenue: '180,900' },
    { name: 'Супермаркет "Угол"', revenue: '150,100' },
    { name: 'Мини-маркет "24/7"', revenue: '89,600' },
    { name: 'Продукты у Дома', revenue: '75,300' }
  ];

  private mainChartInstance?: Chart;
  private rolesChartInstance?: Chart;
  private usersChartInstance?: Chart;

  ngOnInit() {}

  ngAfterViewInit() {
    this.initMainChart();
    this.initRolesChart();
    this.initUsersChart();
  }

  setPeriod(period: string) {
    this.selectedPeriod = period;
  }

  private initMainChart() {
    const ctx = this.mainChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.mainChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['Неделя 1', 'Неделя 2', 'Неделя 3', 'Неделя 4'],
          datasets: [
            {
              label: 'Оборот (BYN)',
              data: [250000, 320000, 280000, 400000],
              borderColor: 'rgba(79, 70, 229, 1)',
              backgroundColor: 'rgba(79, 70, 229, 0.1)',
              fill: true,
              tension: 0.4,
              yAxisID: 'y'
            },
            {
              label: 'Кол-во заказов',
              data: [1200, 1500, 1400, 1900],
              borderColor: 'rgba(34, 197, 94, 1)',
              backgroundColor: 'rgba(34, 197, 94, 0.1)',
              fill: true,
              tension: 0.4,
              yAxisID: 'y1'
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              type: 'linear',
              display: true,
              position: 'left',
              ticks: {
                callback: function(value: any) {
                  return value / 1000 + 'k';
                }
              }
            },
            y1: {
              type: 'linear',
              display: true,
              position: 'right',
              grid: {
                drawOnChartArea: false
              }
            }
          }
        }
      });
    }
  }

  private initRolesChart() {
    const ctx = this.rolesChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.rolesChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['Поставщики', 'Торговые сети'],
          datasets: [{
            data: [520, 734],
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
  }

  private initUsersChart() {
    const ctx = this.usersChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.usersChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['Неделя 1', 'Неделя 2', 'Неделя 3', 'Неделя 4'],
          datasets: [{
            label: 'Новые регистрации',
            data: [5, 12, 8, 25],
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
  }
}
