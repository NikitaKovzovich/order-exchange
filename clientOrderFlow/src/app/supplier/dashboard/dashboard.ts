import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, AfterViewInit {
  @ViewChild('salesChart') salesChart?: ElementRef<HTMLCanvasElement>;

  stats = {
    revenue: '408 000',
    newOrders: 3,
    avgCheck: '12 350',
    inTransit: 5
  };

  pendingOrders = [
    { id: '12345', retailer: 'Сеть Магазинов', amount: '624,00', date: '30.09.2025' },
    { id: '12344', retailer: 'Супермаркет "Угол"', amount: '1 580,00', date: '30.09.2025' },
    { id: '12342', retailer: 'Гипермаркет "Центр"', amount: '3 210,50', date: '29.09.2025' }
  ];

  lowStockProducts = [
    { name: 'Молоко "Деревенское" 3.2% 1л', stock: 8, unit: 'шт.' },
    { name: 'Хлеб "Бородинский" (нарезка)', stock: 3, unit: 'шт.' },
    { name: 'Сыр "Российский" весовой', stock: 2.5, unit: 'кг' }
  ];

  private chart?: Chart;

  ngOnInit() {
  }

  ngAfterViewInit() {
    if (this.salesChart) {
      this.initChart();
    }
  }

  private initChart() {
    const ctx = this.salesChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.chart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['24.09', '25.09', '26.09', '27.09', '28.09', '29.09', '30.09'],
          datasets: [{
            label: 'Выручка (BYN)',
            data: [12000, 19000, 3000, 5000, 22000, 31000, 45000],
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
  }
}
