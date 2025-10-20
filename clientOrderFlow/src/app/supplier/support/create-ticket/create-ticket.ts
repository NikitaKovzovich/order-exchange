import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'supplier-create-ticket',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './create-ticket.html',
  styleUrls: ['./create-ticket.css']
})
export class CreateTicket {
  formData = {
    subject: '',
    description: ''
  };

  constructor(private router: Router) {}

  onFileChange(event: any) {
    console.log('Files selected:', event.target.files);
  }

  onSubmit() {
    console.log('Creating ticket:', this.formData);
    alert('Обращение успешно отправлено!');
    this.router.navigate(['/supplier/support']);
  }

  cancel() {
    this.router.navigate(['/supplier/support']);
  }
}
