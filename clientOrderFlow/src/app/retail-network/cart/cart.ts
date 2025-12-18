import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { AddressService } from '../../services/address.service';
import { AuthService } from '../../services/auth.service';
import { Cart as CartModel, CartItem } from '../../models/api.models';

interface SupplierGroup {
  supplierId: number;
  supplierName: string;
  items: CartItem[];
  deliveryDate: string;
  deliveryAddress: string;
  total: number;
}

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './cart.html',
  styleUrl: './cart.css'
})
export class Cart implements OnInit {
  cart: CartModel | null = null;
  ordersBySupplier: SupplierGroup[] = [];
  deliveryAddresses: string[] = [];
  isLoading: boolean = false;
  isCheckingOut: boolean = false;

  constructor(
    private router: Router,
    private cartService: CartService,
    private addressService: AddressService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadCart();
    this.loadAddresses();
  }

  loadCart() {
    this.isLoading = true;
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cart = cart;
        this.groupBySupplier();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading cart:', error);
        this.isLoading = false;
      }
    });
  }

  loadAddresses() {
    const user = this.authService.getCurrentUser();
    if (user?.companyId) {
      this.addressService.getCompanyAddresses(user.companyId).subscribe({
        next: (addresses) => {
          this.deliveryAddresses = addresses.map(a => a.fullAddress);
        },
        error: (error) => console.error('Error loading addresses:', error)
      });
    }
  }

  groupBySupplier() {
    if (!this.cart) return;

    const groups = new Map<number, SupplierGroup>();

    this.cart.items.forEach(item => {
      if (!groups.has(item.supplierId)) {
        groups.set(item.supplierId, {
          supplierId: item.supplierId,
          supplierName: `Поставщик #${item.supplierId}`,
          items: [],
          deliveryDate: '',
          deliveryAddress: this.deliveryAddresses[0] || '',
          total: 0
        });
      }
      const group = groups.get(item.supplierId)!;
      group.items.push(item);
      group.total += item.totalPrice;
    });

    this.ordersBySupplier = Array.from(groups.values());
  }

  get grandTotal(): number {
    return this.cart?.totalAmount || 0;
  }

  updateQuantity(item: CartItem, quantity: number) {
    if (quantity <= 0) {
      this.removeItem(item);
      return;
    }

    this.cartService.updateItem(item.productId, { quantity }).subscribe({
      next: () => this.loadCart(),
      error: (error) => console.error('Error updating item:', error)
    });
  }

  removeItem(item: CartItem) {
    this.cartService.removeItem(item.productId).subscribe({
      next: () => this.loadCart(),
      error: (error) => console.error('Error removing item:', error)
    });
  }

  clearCart() {
    if (!confirm('Очистить корзину?')) return;

    this.cartService.clearCart().subscribe({
      next: () => {
        this.cart = null;
        this.ordersBySupplier = [];
      },
      error: (error) => console.error('Error clearing cart:', error)
    });
  }

  submitOrder(group: SupplierGroup) {
    if (!group.deliveryDate) {
      alert('Пожалуйста, выберите дату доставки');
      return;
    }

    if (!group.deliveryAddress) {
      alert('Пожалуйста, выберите адрес доставки');
      return;
    }

    this.checkout(group.deliveryAddress, group.deliveryDate);
  }

  checkout(deliveryAddress: string, desiredDeliveryDate: string) {
    this.isCheckingOut = true;

    this.cartService.checkout({
      deliveryAddress,
      desiredDeliveryDate
    }).subscribe({
      next: (response) => {
        this.isCheckingOut = false;
        alert(`Создано заказов: ${response.totalOrders}. Общая сумма: ${response.totalAmount.toFixed(2)} BYN`);
        this.router.navigate(['/retail/orders']);
      },
      error: (error) => {
        console.error('Error during checkout:', error);
        this.isCheckingOut = false;
        alert('Ошибка при оформлении заказа');
      }
    });
  }

  formatPrice(price: number): string {
    return price.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
