import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';

interface VerificationDetails {
  id: number;
  companyName: string;
  legalForm: string;
  taxId: string;
  regDate: string;
  email: string;
  legalAddress: string;
  postalAddress: string;
  directorName: string;
  accountantName: string;
  bankName: string;
  bankBic: string;
  bankAccount: string;
  role: string;
  documents: { name: string; type: string }[];
  submittedAt: string;
  status: string;
}

@Component({
  selector: 'admin-verification',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Header],
  templateUrl: './verification.html',
  styleUrls: ['./verification.css']
})
export class Verification implements OnInit {
  requestId: string = '';
  request: VerificationDetails | null = null;
  showRejectModal: boolean = false;
  rejectionReason: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.requestId = this.route.snapshot.paramMap.get('id') || '';
    this.loadRequest();
  }

  loadRequest() {
    this.request = {
      id: parseInt(this.requestId),
      companyName: 'ОАО "Молочный Мир"',
      legalForm: 'ОАО',
      taxId: '190123456',
      regDate: '10.03.2010',
      email: 'contact@molmir.by',
      legalAddress: '220050, г. Минск, ул. Молочная, д. 5, к. 1',
      postalAddress: '220050, г. Минск, ул. Молочная, д. 5, к. 1',
      directorName: 'Иванов Иван Иванович',
      accountantName: 'Петрова Мария Ивановна',
      bankName: 'ОАО "Технобанк"',
      bankBic: 'TECNBY22',
      bankAccount: 'BY29TECN00000000000123456789',
      role: 'Поставщик',
      documents: [
        { name: 'Свидетельство.pdf', type: 'pdf' },
        { name: 'Логотип.png', type: 'image' },
        { name: 'ЭЦП.sig', type: 'signature' },
        { name: 'Печать.png', type: 'image' }
      ],
      submittedAt: '16.10.2025',
      status: 'pending'
    };
  }

  approve() {
    if (confirm('Вы уверены, что хотите одобрить заявку?')) {
      console.log('Заявка одобрена:', this.requestId);
      alert('Заявка успешно одобрена');
      this.router.navigate(['/admin/verification']);
    }
  }

  openRejectModal() {
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectionReason = '';
  }

  confirmRejection() {
    if (!this.rejectionReason.trim()) {
      alert('Пожалуйста, укажите причину отклонения');
      return;
    }

    console.log('Заявка отклонена:', this.requestId, 'Причина:', this.rejectionReason);
    alert('Заявка отклонена. Компания получит уведомление.');
    this.router.navigate(['/admin/verification']);
  }

  downloadDocument(docName: string) {
    console.log('Скачивание документа:', docName);
    alert('Функция скачивания документа: ' + docName);
  }

  getDocumentIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'pdf': 'M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z',
      'image': 'M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l-1-1a2 2 0 00-2.828 0L4 16',
      'signature': 'M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z'
    };
    return icons[type] || icons['pdf'];
  }
}
