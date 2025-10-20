import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-product-card',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCard implements OnInit {
  productId: string = '';
  currentStock: number = 150;
  newStock: number = 150;
  selectedPhoto: number = 0;

  product = {
    name: 'Молоко "Деревенское" 3.2% 1л',
    sku: 'MLK-001',
    category: 'Молочные продукты',
    description: 'Натуральное коровье молоко с жирностью 3.2%. Проходит бережную пастеризацию, сохраняя все полезные свойства и вкус. Идеально подходит для питья, приготовления каш и выпечки. Упаковка Тетра Пак обеспечивает длительный срок хранения.',
    price: 2.50,
    unit: 'шт',
    vat: '10%',
    weight: 1,
    dimensions: '9x6x19 см',
    mfgDate: '14.10.2025',
    expDate: '10 дней',
    origin: 'Беларусь',
    status: 'published',
    photos: [
      'https://placehold.co/400x400/E2E8F0/4A5568?text=Главное+фото',
      'https://placehold.co/400x400/E2E8F0/4A5568?text=Фото+2',
      'https://placehold.co/400x400/E2E8F0/4A5568?text=Фото+3'
    ]
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.productId = this.route.snapshot.paramMap.get('id') || '';
  }

  selectPhoto(index: number) {
    this.selectedPhoto = index;
  }

  updateStock() {
    this.currentStock = this.newStock;
    alert('Остаток успешно обновлен!');
  }

  editProduct() {
    this.router.navigate(['/supplier/catalog/edit', this.productId]);
  }
}
