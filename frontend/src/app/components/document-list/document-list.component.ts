import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DocumentService, Document, DocumentStatus, PageResponse } from '../../services/document.service';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './document-list.component.html',
  styleUrl: './document-list.component.css'
})
export class DocumentListComponent implements OnInit {
  page = signal<PageResponse<Document> | null>(null);
  loading = signal(false);
  error = signal('');
  filterTitle = signal('');
  filterStatus = signal<DocumentStatus | ''>('');
  currentPage = signal(0);
  pageSize = 10;
  sortField = signal<string>('updatedAt');
  sortDirection = signal<'asc' | 'desc'>('desc');

  docs = computed(() => this.page()?.content ?? []);
  totalPages = computed(() => this.page()?.totalPages ?? 0);
  totalElements = computed(() => this.page()?.totalElements ?? 0);
  sortString = computed(() => `${this.sortField()},${this.sortDirection()}`);

  constructor(private docService: DocumentService) {}

  ngOnInit() {
    this.load();
  }

  onSort(field: string) {
    if (this.sortField() === field) {
      this.sortDirection.update(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set(field);
      this.sortDirection.set(field === 'updatedAt' ? 'desc' : 'asc');
    }
    this.currentPage.set(0);
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    const status = this.filterStatus() || undefined;
    this.docService
      .list(this.filterTitle() || undefined, status, this.currentPage(), this.pageSize, this.sortString())
      .subscribe({
        next: (p) => {
          this.page.set(p);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(err?.error?.message || 'Erro ao carregar documentos.');
          this.loading.set(false);
        }
      });
  }

  onFilter() {
    this.currentPage.set(0);
    this.load();
  }

  goToPage(p: number) {
    this.currentPage.set(p);
    this.load();
  }
}
