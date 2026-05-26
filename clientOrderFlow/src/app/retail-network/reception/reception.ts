import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AcceptanceJournalDetail, AcceptanceJournalSummary, SupplierDirectoryItem } from '../../models/api.models';
import { AcceptanceJournalService } from '../../services/acceptance-journal.service';
import { PartnershipService } from '../../services/partnership.service';

type PeriodPreset = 'all' | 'last7' | 'thisMonth' | 'custom';

@Component({
  selector: 'retail-reception',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reception.html',
  styleUrls: ['./reception.css']
})
export class Reception implements OnInit {
  filterSupplierId: number | null = null;
  filterPeriod: PeriodPreset = 'thisMonth';
  customDateFrom: string = '';
  customDateTo: string = '';

  suppliers: SupplierDirectoryItem[] = [];
  details: AcceptanceJournalDetail[] = [];
  summary: AcceptanceJournalSummary[] = [];
  totalQuantity: number = 0;
  totalAmount: number = 0;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private acceptanceJournalService: AcceptanceJournalService,
    private partnershipService: PartnershipService
  ) {}

  ngOnInit() {
    this.loadSuppliers();
    this.loadJournal();
  }

  loadSuppliers(): void {
    this.partnershipService.getCustomerSuppliers().subscribe({
      next: suppliers => this.suppliers = suppliers.filter(s => s.partnershipStatus === 'ACTIVE'),
      error: error => console.error('Error loading suppliers:', error)
    });
  }

  loadJournal(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const range = this.resolveDateRange();

    this.acceptanceJournalService.getJournal({
      supplierId: this.filterSupplierId ?? undefined,
      dateFrom: range.dateFrom,
      dateTo: range.dateTo
    }).subscribe({
      next: journal => {
        this.details = journal.details;
        this.summary = journal.summary;
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

  onFilterChange(): void {
    this.loadJournal();
  }

  formatDateTime(date: string): string {
    return new Date(date).toLocaleString('ru-RU');
  }

  formatAmount(value: number | undefined): string {
    return Number(value || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  private resolveDateRange(): { dateFrom?: string; dateTo?: string } {
    const today = new Date();
    const formatIso = (d: Date) => d.toISOString().slice(0, 10);

    switch (this.filterPeriod) {
      case 'last7': {
        const from = new Date(today);
        from.setDate(today.getDate() - 7);
        return { dateFrom: formatIso(from), dateTo: formatIso(today) };
      }
      case 'thisMonth': {
        const from = new Date(today.getFullYear(), today.getMonth(), 1);
        return { dateFrom: formatIso(from), dateTo: formatIso(today) };
      }
      case 'custom': {
        return {
          dateFrom: this.customDateFrom || undefined,
          dateTo: this.customDateTo || undefined
        };
      }
      default:
        return {};
    }
  }
}
