import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { SupportService } from '../../../services/support.service';

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
    description: '',
    category: 'TECHNICAL_ISSUE',
    priority: 'NORMAL'
  };
  selectedFiles: File[] = [];
  isSubmitting: boolean = false;

  constructor(
    private router: Router,
    private supportService: SupportService
  ) {}

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFiles = Array.from(input.files || []);
  }

  onSubmit() {
    if (!this.formData.subject || !this.formData.description) {
      return;
    }

    this.isSubmitting = true;
    this.supportService.createTicketMultipart(
      this.formData.subject,
      this.formData.description,
      this.formData.category,
      this.formData.priority,
      this.selectedFiles
    ).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/supplier/support']);
      },
      error: error => {
        console.error('Error creating supplier ticket:', error);
        this.isSubmitting = false;
      }
    });
  }

  cancel() {
    this.router.navigate(['/supplier/support']);
  }
}
