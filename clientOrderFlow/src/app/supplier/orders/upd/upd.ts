import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'supplier-upd',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upd.html',
  styleUrls: ['./upd.css']
})
export class UPD {
  constructor(private router: Router) {}

  printUPD() {
    window.print();
  }

  downloadPDF() {
    alert('Функция скачивания PDF будет реализована');
  }

  goBack() {
    this.router.navigate(['/supplier/orders']);
  }
}

