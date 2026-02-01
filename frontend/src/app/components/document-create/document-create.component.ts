import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { DocumentService, DocumentRequest, DocumentStatus } from '../../services/document.service';

@Component({
  selector: 'app-document-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './document-create.component.html',
  styleUrl: './document-create.component.css'
})
export class DocumentCreateComponent {
  title = '';
  description = '';
  tagsStr = '';
  status: DocumentStatus = 'DRAFT';
  error = signal('');
  loading = signal(false);

  constructor(
    private docService: DocumentService,
    private router: Router
  ) {}

  private toTitleCase(value: string): string {
    return value.toLowerCase().split(' ').map(w => w ? w.charAt(0).toUpperCase() + w.slice(1) : '').join(' ');
  }

  updateTitle(value: string) {
    this.title = this.toTitleCase(value);
  }

  updateTagsStr(value: string) {
    this.tagsStr = value.toUpperCase();
  }

  submit() {
    if (!this.title.trim()) {
      this.error.set('Título é obrigatório.');
      return;
    }
    if (this.title.length > 50) {
      this.error.set('Título deve ter no máximo 50 caracteres.');
      return;
    }
    this.error.set('');
    this.loading.set(true);
    const tags = this.tagsStr ? this.tagsStr.split(',').map(t => t.trim()).filter(t => t.length > 0 && t.length <= 25) : [];
    const body: DocumentRequest = { title: this.title.trim(), description: this.description.trim() || undefined, tags: tags.length ? tags : undefined, status: this.status };
    this.docService.create(body).subscribe({
      next: (doc) => {
        this.loading.set(false);
        this.router.navigate(['/app/documents', doc.id]);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Erro ao criar documento.');
        this.loading.set(false);
      }
    });
  }
}
