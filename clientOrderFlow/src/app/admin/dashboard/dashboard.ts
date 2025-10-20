import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Chart } from 'chart.js';
import { Header } from '../shared/header/header';

interface StatCard {
  title: string;
  value: string | number;
  change: string;
  icon: string;
  trend: 'up' | 'down';
  colorClass: string;
}

interface RegistrationRequest {
  company: string;
  role: string;
  roleClass: string;
  date: string;
  id: number;
}

interface SupportTicket {
  id: string;
  title: string;
  company: string;
  status: string;
  statusClass: string;
}

@Component({
  selector: 'admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, Header],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class Dashboard implements OnInit, AfterViewInit {
  @ViewChild('usersChart') usersChart?: ElementRef<HTMLCanvasElement>;

  stats: StatCard[] = [
    {
      title: 'Всего пользователей',
      value: '1,254',
      change: '+25 за неделю',
      icon: 'users',
      trend: 'up',
      colorClass: 'bg-blue-100 text-blue-600'
    },
    {
      title: 'Заказов (за месяц)',
      value: '5,890',
      change: '+12% к прошлому месяцу',
      icon: 'orders',
      trend: 'up',
      colorClass: 'bg-green-100 text-green-600'
    },
    {
      title: 'Общий оборот (за месяц)',
      value: '1.2M BYN',
      change: '-5% к прошлому месяцу',
      icon: 'revenue',
      trend: 'down',
      colorClass: 'bg-yellow-100 text-yellow-600'
    }
  ];

  registrationRequests: RegistrationRequest[] = [
    { company: 'Молочный Мир', role: 'Поставщик', roleClass: 'bg-green-100 text-green-800', date: '16.10.2025', id: 1 },
    { company: 'Супермаркет "Угол"', role: 'Торговая сеть', roleClass: 'bg-blue-100 text-blue-800', date: '16.10.2025', id: 2 },
    { company: 'ФруктТорг', role: 'Поставщик', roleClass: 'bg-green-100 text-green-800', date: '15.10.2025', id: 3 }
  ];

  supportTickets: SupportTicket[] = [
    { id: 'TKT-105', title: 'Не могу загрузить УПД', company: 'Сеть Магазинов', status: 'Новое', statusClass: 'bg-red-100 text-red-800' },
    { id: 'TKT-104', title: 'Ошибка при смене пароля', company: 'Продукты Оптом', status: 'В работе', statusClass: 'bg-yellow-100 text-yellow-800' },
    { id: 'TKT-102', title: 'Вопрос по API', company: 'Гипермаркет', status: 'Решен', statusClass: 'bg-gray-100 text-gray-800' }
  ];

  private chartInstance?: Chart;

  ngOnInit() {}

  ngAfterViewInit() {
    this.initUsersChart();
  }

  private initUsersChart() {
    const ctx = this.usersChart?.nativeElement.getContext('2d');
    if (ctx) {
      this.chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['Неделя 1', 'Неделя 2', 'Неделя 3', 'Неделя 4'],
          datasets: [{
            label: 'Новые пользователи',
            data: [5, 12, 8, 25],
            backgroundColor: 'rgba(79, 70, 229, 0.1)',
            borderColor: 'rgba(79, 70, 229, 1)',
            borderWidth: 2,
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
