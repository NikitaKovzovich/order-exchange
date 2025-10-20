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
    digitalSignature: null as File | null,
    stamp: null as File | null,
    termsAccepted: false
  };

  deliveryAddresses: DeliveryAddress[] = [
    { id: 1, address: '' }
  ];

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
