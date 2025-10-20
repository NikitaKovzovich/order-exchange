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
    corrAccount: '',
    paymentTerms: 'prepaid',
    logo: null as File | null,
    registrationCert: null as File | null,
    digitalSignature: null as File | null,
    stamp: null as File | null,
    termsAccepted: false
  };

  constructor(private router: Router) {}

  nextStep() {
    if (this.currentStep < 4) {
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
