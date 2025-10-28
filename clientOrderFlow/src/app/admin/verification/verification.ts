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
    const id = parseInt(this.requestId);

    // Данные для разных заявок с разными статусами
    const requestsData: { [key: number]: VerificationDetails } = {
      1: { // Ожидает проверки - Молочный Мир
        id: 1,
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
          { name: 'Логотип.png', type: 'image' }
        ],
        submittedAt: '16.10.2025',
        status: 'pending'
      },
      2: { // Ожидает проверки - Супермаркет "Угол"
        id: 2,
        companyName: 'ООО "Супермаркет Угол"',
        legalForm: 'ООО',
        taxId: '199876543',
        regDate: '15.05.2018',
        email: 'info@ugol.by',
        legalAddress: '220012, г. Минск, пр. Независимости, д. 120',
        postalAddress: '220012, г. Минск, пр. Независимости, д. 120',
        directorName: 'Сидоров Петр Петрович',
        accountantName: 'Иванова Ольга Сергеевна',
        bankName: 'ЗАО "БелБизнесБанк"',
        bankBic: 'BBBSBY22',
        bankAccount: 'BY45BBBS00000000000987654321',
        role: 'Торговая сеть',
        documents: [
          { name: 'Свидетельство_регистрации.pdf', type: 'pdf' },
          { name: 'Логотип_компании.png', type: 'image' }
        ],
        submittedAt: '16.10.2025',
        status: 'pending'
      },
      3: { // Одобрена - Продукты Оптом
        id: 3,
        companyName: 'ИП "Продукты Оптом"',
        legalForm: 'ИП',
        taxId: '191234567',
        regDate: '22.01.2015',
        email: 'sales@produkty-opt.by',
        legalAddress: '220030, г. Минск, ул. Торговая, д. 10',
        postalAddress: '220030, г. Минск, ул. Торговая, д. 10',
        directorName: 'Кузнецов Алексей Владимирович',
        accountantName: 'Смирнова Анна Ивановна',
        bankName: 'ОАО "Приорбанк"',
        bankBic: 'PJCBBY2X',
        bankAccount: 'BY12PJCB00000000000111222333',
        role: 'Поставщик',
        documents: [
          { name: 'Свидетельство.pdf', type: 'pdf' },
          { name: 'Логотип.jpg', type: 'image' }
        ],
        submittedAt: '15.10.2025',
        status: 'approved'
      },
      4: { // Отклонена - Быстрый Магазинчик
        id: 4,
        companyName: 'ООО "Быстрый Магазинчик"',
        legalForm: 'ООО',
        taxId: '194567890',
        regDate: '05.08.2020',
        email: 'contact@fast-shop.by',
        legalAddress: '220020, г. Минск, ул. Короткая, д. 3',
        postalAddress: '220020, г. Минск, ул. Короткая, д. 3',
        directorName: 'Новиков Сергей Михайлович',
        accountantName: 'Федорова Елена Петровна',
        bankName: 'ОАО "БПС-Сбербанк"',
        bankBic: 'BPSBBY2X',
        bankAccount: 'BY88BPSB00000000000555666777',
        role: 'Торговая сеть',
        documents: [
          { name: 'Свидетельство.pdf', type: 'pdf' },
          { name: 'Лого.png', type: 'image' }
        ],
        submittedAt: '15.10.2025',
        status: 'rejected'
      },
      5: { // Ожидает проверки - ФруктТорг
        id: 5,
        companyName: 'ЧУП "ФруктТорг"',
        legalForm: 'ЧУП',
        taxId: '192345678',
        regDate: '18.03.2017',
        email: 'info@frukt-torg.by',
        legalAddress: '220040, г. Минск, ул. Фруктовая, д. 25',
        postalAddress: '220040, г. Минск, ул. Фруктовая, д. 25',
        directorName: 'Орлов Дмитрий Николаевич',
        accountantName: 'Волкова Ирина Андреевна',
        bankName: 'ОАО "Белагропромбанк"',
        bankBic: 'BAPBBY2X',
        bankAccount: 'BY33BAPB00000000000444555666',
        role: 'Поставщик',
        documents: [
          { name: 'Регистрация.pdf', type: 'pdf' },
          { name: 'Лого.png', type: 'image' }
        ],
        submittedAt: '15.10.2025',
        status: 'pending'
      },
      6: { // Одобрена - Мясной Двор
        id: 6,
        companyName: 'ООО "Мясной Двор"',
        legalForm: 'ООО',
        taxId: '193456789',
        regDate: '12.06.2016',
        email: 'sales@myaso-dvor.by',
        legalAddress: '220050, г. Минск, ул. Мясная, д. 8',
        postalAddress: '220050, г. Минск, ул. Мясная, д. 8',
        directorName: 'Морозов Игорь Сергеевич',
        accountantName: 'Лебедева Светлана Викторовна',
        bankName: 'ОАО "БелВЭБ"',
        bankBic: 'BVEBBY22',
        bankAccount: 'BY77BVEB00000000000222333444',
        role: 'Поставщик',
        documents: [
          { name: 'Свидетельство_ООО.pdf', type: 'pdf' },
          { name: 'Логотип_МясДвор.png', type: 'image' }
        ],
        submittedAt: '14.10.2025',
        status: 'approved'
      },
      7: { // Одобрена - Сеть Продуктов
        id: 7,
        companyName: 'ОАО "Сеть Продуктов"',
        legalForm: 'ОАО',
        taxId: '195678901',
        regDate: '09.09.2014',
        email: 'contact@setprod.by',
        legalAddress: '220090, г. Минск, пр. Победителей, д. 50',
        postalAddress: '220090, г. Минск, пр. Победителей, д. 50',
        directorName: 'Павлов Андрей Валерьевич',
        accountantName: 'Николаева Татьяна Дмитриевна',
        bankName: 'ОАО "Беларусбанк"',
        bankBic: 'BBSBY22',
        bankAccount: 'BY99BBBY00000000000888999000',
        role: 'Торговая сеть',
        documents: [
          { name: 'Свидетельство_ОАО.pdf', type: 'pdf' },
          { name: 'Логотип_СетьПродуктов.jpg', type: 'image' }
        ],
        submittedAt: '14.10.2025',
        status: 'approved'
      },
      8: { // Отклонена - ТоварыОпт
        id: 8,
        companyName: 'ИП "ТоварыОпт"',
        legalForm: 'ИП',
        taxId: '196789012',
        regDate: '25.11.2019',
        email: 'info@tovary-opt.by',
        legalAddress: '220060, г. Минск, ул. Складская, д. 15',
        postalAddress: '220060, г. Минск, ул. Складская, д. 15',
        directorName: 'Егоров Максим Александрович',
        accountantName: 'Соколова Марина Олеговна',
        bankName: 'ОАО "БНБ-Банк"',
        bankBic: 'BNBSBY22',
        bankAccount: 'BY11BNBB00000000000123987456',
        role: 'Поставщик',
        documents: [
          { name: 'Регистрация_ИП.pdf', type: 'pdf' },
          { name: 'Лого_ТоварыОпт.png', type: 'image' }
        ],
        submittedAt: '13.10.2025',
        status: 'rejected'
      }
    };

    this.request = requestsData[id] || requestsData[1];
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
