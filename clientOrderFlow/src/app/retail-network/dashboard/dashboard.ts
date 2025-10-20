import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Chart } from 'chart.js';

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
    totalExpenses: '28,450',
    totalOrders: 18,
    activeSuppliers: 5,
    avgOrderAmount: '1,580'
  };

  recentOrders = [
    { id: '10234', supplier: 'Продукты Оптом', amount: '624.00', status: 'in-transit', statusLabel: 'В пути' },
    { id: '10233', supplier: 'Молочный Мир', amount: '1,250.00', status: 'awaiting-payment', statusLabel: 'Ожидает оплаты' },
    { id: '10232', supplier: 'ХлебПром', amount: '350.50', status: 'delivered', statusLabel: 'Доставлен' }
  ];

  private dynamicsChart?: Chart;
  private structureChart?: Chart;

  ngOnInit() {}

  ngAfterViewInit() {
    this.initDynamicsChart();
    this.initStructureChart();
  }

  private initDynamicsChart() {
    const ctx = this.expensesDynamicsChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.dynamicsChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ', 'ВС'],
          datasets: [{
            label: 'Расходы (BYN)',
            data: [1200, 1900, 3000, 5000, 2200, 3100, 4500],
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
  }

  private initStructureChart() {
    const ctx = this.expensesChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.structureChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['Молочные продукты', 'Хлебобулочные', 'Сыры', 'Напитки', 'Другое'],
          datasets: [{
            data: [35, 25, 15, 10, 15],
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
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'awaiting-payment': 'text-yellow-800 bg-yellow-200',
      'in-transit': 'text-blue-600 bg-blue-200',
      'delivered': 'text-green-800 bg-green-200'
    };
    return classes[status] || 'text-gray-600 bg-gray-200';
  }
}
