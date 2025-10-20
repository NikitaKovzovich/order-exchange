import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {
  @Input() pageTitle: string = 'Главная панель';

  companyName: string = 'Продукты Оптом';
  companyLogo: string = 'https://placehold.co/100x100/E2E8F0/4A5568?text=Лого';
  hasNotifications: boolean = true;
}
