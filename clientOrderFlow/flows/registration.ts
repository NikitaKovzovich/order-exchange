import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

interface DeliveryAddress {
  id: number;
  address: string;
}

@Component({
  selector: 'app-retail-registration',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registration.html',
  styleUrl: './registration.css'
})
export class Registration {
  currentStep: number = 1;
  showSuccess: boolean = false;
  userType: 'supplier' | 'retail' | '' = '';

  formData = {
    email: '',
    password: '',
    passwordConfirm: '',
    companyName: '',
    legalForm: 'ООО',
    regDate: '',
    taxId: '',
    legalAddress: '',
    postalAddress: '',
    contactPhone: '',
    logo: null as File | null,
    registrationCert: null as File | null,
    termsAccepted: false
  };

  deliveryAddresses: DeliveryAddress[] = [
    { id: 1, address: '' }
  ];

  constructor(private router: Router) {
    // Проверяем, нужно ли начать со 2-го шага
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras?.state?.['startFromStep2']) {
      this.currentStep = 2;
      this.userType = 'retail';
    }
  }

  selectUserType(type: 'supplier' | 'retail') {
    this.userType = type;
  }

  nextStep() {
    // На первом шаге перенаправляем на соответствующую форму регистрации
    if (this.currentStep === 1) {
      if (!this.userType) {
        alert('Пожалуйста, выберите тип регистрации');
        return;
      }

      if (this.userType === 'supplier') {
        // Перенаправляем на форму поставщика, начиная со 2-го шага
        this.router.navigate(['/supplier/auth/registration'], { state: { startFromStep2: true } });
        return;
      }
      // Если выбрана торговая сеть, просто переходим на следующий шаг
    }

    if (this.currentStep < 5) {
      this.currentStep++;
    } else {
      this.onSubmit();
    }
  }

  previousStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  addDeliveryAddress() {
    const newId = this.deliveryAddresses.length + 1;
    this.deliveryAddresses.push({ id: newId, address: '' });
  }

  removeDeliveryAddress(id: number) {
    if (this.deliveryAddresses.length > 1) {
      this.deliveryAddresses = this.deliveryAddresses.filter(addr => addr.id !== id);
    }
  }

  onFileSelect(event: any, field: string) {
    const file = event.target.files[0];
    if (file) {
      (this.formData as any)[field] = file;
    }
  }

  onSubmit() {
    console.log('Retail registration data:', this.formData, this.deliveryAddresses);
    this.showSuccess = true;
  }

  returnToHome() {
    this.router.navigate(['/']);
  }
}
