import { Injectable } from '@angular/core';
import { catchError, of } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import { GeneratedDocument, OrderDocument } from '../models/api.models';
import { DocumentService } from './document.service';
import { OrderService } from './order.service';

export type DownloadAllDocumentsStatus = 'success' | 'partial' | 'empty' | 'error';

export interface DownloadAllOrderDocumentsResult {
  status: DownloadAllDocumentsStatus;
  totalRequested: number;
  downloadedCount: number;
  failedFiles: string[];
  sourceErrors: string[];
  archiveName?: string;
}

interface DownloadableOrderFile {
  zipPath: string;
  download: () => Promise<Blob>;
}

@Injectable({
  providedIn: 'root'
})
export class OrderDocumentDownloadService {
  constructor(
    private documentService: DocumentService,
    private orderService: OrderService
  ) {}

  async downloadAllForOrder(orderId: number, orderNumber?: string): Promise<DownloadAllOrderDocumentsResult> {
    const sourceErrors: string[] = [];

    const [generatedDocuments, orderDocuments] = await Promise.all([
      firstValueFrom(
        this.documentService.getGeneratedDocumentsByOrder(orderId).pipe(
          catchError(error => {
            console.error('Error loading generated documents:', error);
            sourceErrors.push('generated');
            return of([] as GeneratedDocument[]);
          })
        )
      ),
      firstValueFrom(
        this.orderService.getOrderDocuments(orderId).pipe(
          catchError(error => {
            console.error('Error loading order documents:', error);
            sourceErrors.push('uploaded');
            return of([] as OrderDocument[]);
          })
        )
      )
    ]);

    const files = this.buildDownloadableFiles(generatedDocuments, orderDocuments);

    if (!files.length) {
      return {
        status: sourceErrors.length ? 'error' : 'empty',
        totalRequested: 0,
        downloadedCount: 0,
        failedFiles: [],
        sourceErrors
      };
    }

    const failedFiles: string[] = [];
    const zipEntries = await Promise.all(files.map(async file => {
      try {
        const blob = await file.download();
        return {
          path: file.zipPath,
          data: new Uint8Array(await blob.arrayBuffer())
        };
      } catch (error) {
        console.error(`Error downloading file ${file.zipPath}:`, error);
        failedFiles.push(file.zipPath.split('/').pop() || file.zipPath);
        return null;
      }
    }));

    const successfulEntries = zipEntries.filter(entry => entry !== null);

    const downloadedCount = files.length - failedFiles.length;

    if (downloadedCount === 0) {
      return {
        status: 'error',
        totalRequested: files.length,
        downloadedCount,
        failedFiles,
        sourceErrors
      };
    }

    const { zip } = await import('fflate');
    const archivePayload: Record<string, Uint8Array> = {};
    successfulEntries.forEach(entry => {
      if (!entry) {
        return;
      }

      archivePayload[entry.path] = entry.data;
    });
    const archiveName = this.buildArchiveName(orderId, orderNumber);
    const archive = await new Promise<Uint8Array>((resolve, reject) => {
      zip(archivePayload, { level: 6 }, (error, data) => {
        if (error) {
          reject(error);
          return;
        }

        resolve(data);
      });
    });
    const archiveBytes = new Uint8Array(archive.byteLength);
    archiveBytes.set(archive);
    this.saveFile(new Blob([archiveBytes], { type: 'application/zip' }), archiveName);

    return {
      status: failedFiles.length || sourceErrors.length ? 'partial' : 'success',
      totalRequested: files.length,
      downloadedCount,
      failedFiles,
      sourceErrors,
      archiveName
    };
  }

  private buildDownloadableFiles(generatedDocuments: GeneratedDocument[], orderDocuments: OrderDocument[]): DownloadableOrderFile[] {
    const usedPaths = new Set<string>();

    return [
      ...generatedDocuments.map(document => ({
        zipPath: this.buildUniquePath('generated', this.buildGeneratedDocumentName(document), usedPaths),
        download: () => firstValueFrom(this.documentService.downloadGeneratedDocument(document.id))
      })),
      ...orderDocuments.map(document => ({
        zipPath: this.buildUniquePath('uploaded', this.buildOrderDocumentName(document), usedPaths),
        download: () => firstValueFrom(this.documentService.downloadDocument(document.id))
      }))
    ];
  }

  private buildGeneratedDocumentName(document: GeneratedDocument): string {
    const baseName = `${document.templateType.toLowerCase()}_${document.documentNumber}.pdf`;
    return this.sanitizeFileName(baseName);
  }

  private buildOrderDocumentName(document: OrderDocument): string {
    return this.sanitizeFileName(
      document.originalFilename
      || document.fileName
      || document.documentName
      || `document-${document.id}`
    );
  }

  private buildArchiveName(orderId: number, orderNumber?: string): string {
    const suffix = this.sanitizeFileName(orderNumber || `order-${orderId}`);
    return `order_${suffix}_documents.zip`;
  }

  private buildUniquePath(directory: string, fileName: string, usedPaths: Set<string>): string {
    const normalizedDirectory = directory.trim();
    const extensionIndex = fileName.lastIndexOf('.');
    const name = extensionIndex >= 0 ? fileName.slice(0, extensionIndex) : fileName;
    const extension = extensionIndex >= 0 ? fileName.slice(extensionIndex) : '';

    let counter = 1;
    let candidate = `${normalizedDirectory}/${fileName}`;

    while (usedPaths.has(candidate)) {
      counter += 1;
      candidate = `${normalizedDirectory}/${name} (${counter})${extension}`;
    }

    usedPaths.add(candidate);
    return candidate;
  }

  private sanitizeFileName(value: string): string {
    const sanitized = value
      .trim()
      .replace(/[<>:"/\\|?*\x00-\x1F]/g, '_')
      .replace(/\s+/g, ' ')
      .slice(0, 160);

    return sanitized || 'document';
  }

  private saveFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = window.document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }
}

