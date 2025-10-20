import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface ReceptionRecord {
  date: string;
  supplier: string;
  productName: string;
  quantity: number;
  unit: string;
  price: number;
  total: number;
  responsible: string;
}

@Component({
  selector: 'retail-reception',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reception.html',
  styleUrls: ['./reception.css']
})
export class Reception implements OnInit {
  filterDate: string = '';
  filterSupplier: string = '';

  receptionRecords: ReceptionRecord[] = [
    {
      date: '16.10.2025',
      supplier: 'Продукты Оптом',
      productName: 'Молоко "Деревенское" 3.2% 1л',
      quantity: 100,
      unit: 'шт',
      price: 2.50,
      total: 250.00,
      responsible: 'Иванов И.И.'
    },
    {
      date: '16.10.2025',
      supplier: 'Продукты Оптом',
      productName: 'Хлеб "Бородинский" (нарезка)',
      quantity: 50,
      unit: 'шт',
      price: 1.80,
      total: 90.00,
      responsible: 'Иванов И.И.'
    },
    {
      date: '15.10.2025',
      supplier: 'Молочный Мир',
      productName: 'Сыр "Российский" весовой',
      quantity: 10,
      unit: 'кг',
      price: 18.00,
      total: 180.00,
      responsible: 'Петров П.П.'
    },
    {
      date: '15.10.2025',
      supplier: 'ФруктТорг',
      productName: 'Яблоки "Гала" свежие',
      quantity: 50,
      unit: 'кг',
      price: 3.50,
      total: 175.00,
      responsible: 'Сидоров С.С.'
    }
  ];

  totalItems: number = 0;
  totalQuantity: number = 0;
  totalAmount: number = 0;

  ngOnInit() {
    this.calculateTotals();
  }

  calculateTotals() {
    this.totalItems = this.receptionRecords.length;
    this.totalQuantity = this.receptionRecords.reduce((sum, record) => sum + record.quantity, 0);
    this.totalAmount = this.receptionRecords.reduce((sum, record) => sum + record.total, 0);
  }
}
