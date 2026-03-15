import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AcceptanceJournalDetail, AcceptanceJournalSummary } from '../../models/api.models';
import { AcceptanceJournalService } from '../../services/acceptance-journal.service';

@Component({
  selector: 'retail-reception',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reception.html',
  styleUrls: ['./reception.css']
})
export class Reception implements OnInit {
  filterDate: string = '';
  filterSupplier: string = '';
  details: AcceptanceJournalDetail[] = [];
  summary: AcceptanceJournalSummary[] = [];
  totalItems: number = 0;
  totalQuantity: number = 0;
  totalAmount: number = 0;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private acceptanceJournalService: AcceptanceJournalService) {}

  ngOnInit() {
    this.loadJournal();
  }

  loadJournal(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.acceptanceJournalService.getJournal({
      supplierId: this.filterSupplier ? Number(this.filterSupplier) : undefined,
      dateFrom: this.filterDate || undefined
    }).subscribe({
      next: journal => {
        this.details = journal.details;
        this.summary = journal.summary;
        this.totalItems = journal.details.length;
        this.totalQuantity = Number(journal.grandTotalQuantity || 0);
        this.totalAmount = Number(journal.grandTotalAmount || 0);
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading acceptance journal:', error);
        this.errorMessage = 'Не удалось загрузить журнал приёмки';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    this.loadJournal();
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('ru-RU');
  }

  formatDateTime(date: string): string {
    return new Date(date).toLocaleString('ru-RU');
  }
}
