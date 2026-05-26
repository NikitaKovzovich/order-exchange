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

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
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
  notification: UiNotification | null = null;

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
    if (!this.cart) {
      this.ordersBySupplier = [];
      return;
    }

    const previous = new Map(this.ordersBySupplier.map(group => [group.supplierId, group]));
    const groups = new Map<number, SupplierGroup>();

    this.cart.items.forEach(item => {
      if (!groups.has(item.supplierId)) {
        const prior = previous.get(item.supplierId);
        groups.set(item.supplierId, {
          supplierId: item.supplierId,
          supplierName: item.supplierName || `Поставщик #${item.supplierId}`,
          items: [],
          deliveryDate: prior?.deliveryDate || '',
          deliveryAddress: prior?.deliveryAddress || this.deliveryAddresses[0] || '',
          total: 0
        });
      }
      const group = groups.get(item.supplierId)!;
      group.items.push(item);
      group.total += item.totalPrice + (item.vatAmount || 0);
    });

    this.ordersBySupplier = Array.from(groups.values());
  }

  get grandTotal(): number {
    return (this.cart?.totalAmount || 0) + (this.cart?.totalVat || 0);
  }

  get totalPositions(): number {
    return this.ordersBySupplier.reduce((sum, group) => sum + group.items.length, 0);
  }

  get pluralizedOrders(): string {
    return this.pluralize(this.ordersBySupplier.length, 'заказ', 'заказа', 'заказов');
  }

  get pluralizedPositions(): string {
    return this.pluralize(this.totalPositions, 'позиция', 'позиции', 'позиций');
  }

  private pluralize(count: number, one: string, few: string, many: string): string {
    const mod10 = count % 10;
    const mod100 = count % 100;
    if (mod10 === 1 && mod100 !== 11) return `${count} ${one}`;
    if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return `${count} ${few}`;
    return `${count} ${many}`;
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

    this.cartService.clearCart().subscribe({
      next: () => {
        this.cart = null;
        this.ordersBySupplier = [];
      },
      error: (error) => console.error('Error clearing cart:', error)
    });
  }

  private validateGroup(group: SupplierGroup): string | null {
    if (!group.deliveryDate) {
      return `Выберите дату доставки для заказа поставщику «${group.supplierName}».`;
    }
    if (!group.deliveryAddress) {
      return `Выберите адрес доставки для заказа поставщику «${group.supplierName}».`;
    }
    return null;
  }

  submitOrder(group: SupplierGroup) {
    const validationError = this.validateGroup(group);
    if (validationError) {
      this.notification = { type: 'warning', message: validationError };
      return;
    }

    this.notification = null;
    this.isCheckingOut = true;

    this.cartService.checkoutSupplier(group.supplierId, {
      deliveryAddress: group.deliveryAddress,
      desiredDeliveryDate: group.deliveryDate
    }).subscribe({
      next: () => {
        this.isCheckingOut = false;
        this.notification = {
          type: 'success',
          message: `Заказ отправлен поставщику «${group.supplierName}». Статус: «Ожидает подтверждения».`
        };
        this.cartService.getCart().subscribe({
          next: (cart) => {
            this.cart = cart;
            this.groupBySupplier();
            if (this.ordersBySupplier.length === 0) {
              this.router.navigate(['/retail/orders']);
            }
          },
          error: () => this.loadCart()
        });
      },
      error: (error) => {
        console.error('Error during checkout:', error);
        this.isCheckingOut = false;
        this.notification = { type: 'error', message: 'Ошибка при оформлении заказа.' };
      }
    });
  }

  submitAllOrders() {
    if (this.ordersBySupplier.length === 0) {
      return;
    }

    for (const group of this.ordersBySupplier) {
      const validationError = this.validateGroup(group);
      if (validationError) {
        this.notification = { type: 'warning', message: validationError };
        return;
      }
    }

    this.notification = null;
    this.isCheckingOut = true;
    this.submitGroupsSequentially([...this.ordersBySupplier], 0);
  }

  private submitGroupsSequentially(groups: SupplierGroup[], index: number) {
    if (index >= groups.length) {
      this.isCheckingOut = false;
      this.cartSubjectReset();
      this.router.navigate(['/retail/orders']);
      return;
    }

    const group = groups[index];
    this.cartService.checkoutSupplier(group.supplierId, {
      deliveryAddress: group.deliveryAddress,
      desiredDeliveryDate: group.deliveryDate
    }).subscribe({
      next: () => this.submitGroupsSequentially(groups, index + 1),
      error: (error) => {
        console.error('Error during checkout:', error);
        this.isCheckingOut = false;
        this.notification = {
          type: 'error',
          message: `Ошибка при отправке заказа поставщику «${group.supplierName}».`
        };
        this.loadCart();
      }
    });
  }

  private cartSubjectReset() {
    this.cart = null;
    this.ordersBySupplier = [];
  }

  formatPrice(price: number): string {
    return price.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
