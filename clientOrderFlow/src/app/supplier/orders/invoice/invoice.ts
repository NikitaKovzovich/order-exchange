import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'supplier-invoice',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invoice.html',
  styleUrls: ['./invoice.css']
})
export class Invoice {
  constructor(private router: Router) {}

  printInvoice() {
    window.print();
  }

  downloadPDF() {
    alert('Функция скачивания PDF будет реализована');
  }

  goBack() {
    this.router.navigate(['/supplier/orders']);
  }
}
