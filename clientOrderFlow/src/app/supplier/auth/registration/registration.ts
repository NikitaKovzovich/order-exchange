import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registration',
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
    legalForm: 'ИП',
    regDate: '',
    taxId: '',
    legalAddress: '',
    postalAddress: '',
    shippingAddress: '',
    contactPhone: '',
    directorName: '',
    accountantName: '',
    bankName: '',
    bankBic: '',
    bankAccount: '',
    paymentTerms: 'prepaid',
    logo: null as File | null,
    registrationCert: null as File | null,
    termsAccepted: false
  };

  constructor(private router: Router) {
    // Проверяем, нужно ли начать со 2-го шага
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras?.state?.['startFromStep2']) {
      this.currentStep = 2;
      this.userType = 'supplier';
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

      if (this.userType === 'retail') {
        // Перенаправляем на форму торговой сети, начиная со 2-го шага
        this.router.navigate(['/retail/auth/registration'], { state: { startFromStep2: true } });
        return;
      }
      // Если выбран поставщик, просто переходим на следующий шаг
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

  onFileSelect(event: any, field: string) {
    const file = event.target.files[0];
    if (file) {
      (this.formData as any)[field] = file;
    }
  }

  onSubmit() {
    console.log('Registration data:', this.formData);
    this.showSuccess = true;
  }

  returnToHome() {
    this.router.navigate(['/']);
  }
}
