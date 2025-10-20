import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-retail-header',
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {
  @Input() companyName: string = 'Торговая сеть';
  @Input() hasNotifications: boolean = false;
  @Input() cartItemsCount: number = 0;

  get hasCartItems(): boolean {
    return this.cartItemsCount > 0;
  }
}
