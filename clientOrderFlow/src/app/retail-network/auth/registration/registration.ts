import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RegistrationService, RegistrationResponse } from '../../../services/registration.service';

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
  errors: { [key: string]: string } = {};
  isSubmitting: boolean = false;

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
    shippingAddress: '',
    contactPhone: '',
    termsAccepted: false
  };

  deliveryAddresses: DeliveryAddress[] = [
    { id: 1, address: '' }
  ];

  // Файлы - только 2 для торговой сети
  files = {
    logo: null as File | null,
    registrationCert: null as File | null
  };

  // Имена выбранных файлов для отображения
  fileNames = {
    logo: '',
    registrationCert: ''
  };

  constructor(
    private router: Router,
    private registrationService: RegistrationService
  ) {
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras?.state?.['startFromStep2']) {
      this.currentStep = 2;
      this.userType = 'retail';
    }
  }

  selectUserType(type: 'supplier' | 'retail') {
    this.userType = type;
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

  validateStep(step: number): boolean {
    this.errors = {};

    switch(step) {
      case 1:
        if (!this.userType) {
          this.errors['userType'] = 'Пожалуйста, выберите тип регистрации';
          return false;
        }
        break;

      case 2:
        if (!this.formData.email) {
          this.errors['email'] = 'Email обязателен';
          return false;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.formData.email)) {
          this.errors['email'] = 'Введите корректный email';
          return false;
        }
        if (!this.formData.password) {
          this.errors['password'] = 'Пароль обязателен';
          return false;
        }
        if (this.formData.password.length < 8) {
          this.errors['password'] = 'Пароль должен содержать минимум 8 символов';
          return false;
        }
        if (this.formData.password !== this.formData.passwordConfirm) {
          this.errors['passwordConfirm'] = 'Пароли не совпадают';
          return false;
        }
        break;

      case 3:
        if (!this.formData.companyName) {
          this.errors['companyName'] = 'Название компании обязательно';
          return false;
        }
        if (!this.formData.taxId) {
          this.errors['taxId'] = 'УНП обязателен';
          return false;
        }
        if (!/^\d{9}$/.test(this.formData.taxId)) {
          this.errors['taxId'] = 'УНП должен содержать 9 цифр';
          return false;
        }
        if (!this.formData.legalAddress) {
          this.errors['legalAddress'] = 'Юридический адрес обязателен';
          return false;
        }
        if (!this.formData.contactPhone) {
          this.errors['contactPhone'] = 'Контактный телефон обязателен';
          return false;
        }
        break;

      case 4:
        const hasValidAddress = this.deliveryAddresses.some(addr => addr.address.trim() !== '');
        if (!hasValidAddress) {
          this.errors['deliveryAddresses'] = 'Необходимо указать хотя бы один адрес доставки';
          return false;
        }
        break;

      case 5:
        if (!this.formData.termsAccepted) {
          this.errors['termsAccepted'] = 'Необходимо принять условия использования';
          return false;
        }
        break;
    }

    return true;
  }

  nextStep() {
    if (this.currentStep === 1) {
      if (!this.validateStep(1)) {
        return;
      }

      if (this.userType === 'supplier') {
        this.router.navigate(['/supplier/auth/registration'], {
          state: { startFromStep2: true },
          replaceUrl: false
        });
        return;
      }
      // Если выбрана торговая сеть, продолжаем в текущей форме
    }

    if (!this.validateStep(this.currentStep)) {
      return;
    }

    if (this.currentStep < 5) {
      this.currentStep++;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      this.onSubmit();
    }
  }

  previousStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  onFileSelect(event: any, field: keyof typeof this.files) {
    const file = event.target.files?.[0];
    if (file) {
      this.files[field] = file;
      this.fileNames[field] = file.name;
    }
  }

  onSubmit() {
    if (!this.validateStep(5)) {
      return;
    }

    this.isSubmitting = true;
    this.errors = {};

    const formDataToSend = new FormData();

    // Обязательные поля
    formDataToSend.append('type', 'RETAIL_CHAIN');
    formDataToSend.append('email', this.formData.email);
    formDataToSend.append('password', this.formData.password);
    formDataToSend.append('passwordConfirm', this.formData.passwordConfirm);
    formDataToSend.append('legalName', this.formData.companyName);
    formDataToSend.append('legalForm', this.mapLegalForm(this.formData.legalForm));
    formDataToSend.append('taxId', this.formData.taxId);
    formDataToSend.append('registrationDate', this.formData.regDate || new Date().toISOString().split('T')[0]);
    formDataToSend.append('contactPhone', this.formData.contactPhone);
    formDataToSend.append('legalAddress', this.formData.legalAddress);

    // Опциональные поля
    if (this.formData.postalAddress) {
      formDataToSend.append('postalAddress', this.formData.postalAddress);
    }
    if (this.formData.shippingAddress) {
      formDataToSend.append('shippingAddress', this.formData.shippingAddress);
    }

    // Адреса доставки как JSON массив
    const validAddresses = this.deliveryAddresses
      .map(addr => addr.address.trim())
      .filter(addr => addr !== '');

    if (validAddresses.length > 0) {
      formDataToSend.append('deliveryAddresses', JSON.stringify(validAddresses));
    }

    // Файлы (только 2 для торговой сети)
    if (this.files.logo) {
      formDataToSend.append('logo', this.files.logo, this.files.logo.name);
    }
    if (this.files.registrationCert) {
      formDataToSend.append('registrationCertificate', this.files.registrationCert, this.files.registrationCert.name);
    }

    this.registrationService.registerRetail(formDataToSend).subscribe({
      next: (response: RegistrationResponse) => {
        console.log('Registration successful:', response);
        this.isSubmitting = false;
        this.showSuccess = true;

        if (response.token) {
          localStorage.setItem('jwt_token', response.token);
        }
      },
      error: (error: any) => {
        console.error('Registration error:', error);
        this.isSubmitting = false;
        this.errors['submit'] = error.error?.message || error.error?.error || 'Произошла ошибка при регистрации. Попробуйте еще раз.';
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  private mapLegalForm(russianForm: string): string {
    const mapping: { [key: string]: string } = {
      'ИП': 'IE',
      'ООО': 'LLC',
      'ОАО': 'OJSC',
      'ЗАО': 'CJSC',
      'ПАО': 'PJSC',
      'ЧУП': 'PUE'
    };
    return mapping[russianForm] || 'LLC';
  }

  returnToHome() {
    this.router.navigate(['/']);
  }
}
