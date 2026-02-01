import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DocumentService, Document, DocumentVersion, DocumentRequest, DocumentStatus } from '../../services/document.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-document-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './document-detail.component.html',
  styleUrl: './document-detail.component.css'
})
export class DocumentDetailComponent implements OnInit {
  doc = signal<Document | null>(null);
  versions = signal<DocumentVersion[]>([]);
  loading = signal(false);
  error = signal('');
  uploadFile = signal<File | null>(null);
  uploadError = signal('');
  editing = signal(false);
  editSaving = signal(false);
  showDeleteModal = signal(false);
  editTitle = '';
  editDescription = '';
  editTagsStr = '';
  editStatus: DocumentStatus = 'DRAFT';

  docId = computed(() => Number(this.route.snapshot.paramMap.get('id')));

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private docService: DocumentService,
    public authService: AuthService
  ) {}

  isOwner(): boolean {
    const doc = this.doc();
    const userId = this.authService.user()?.userId;
    return !!doc && !!userId && doc.ownerId === userId;
  }

  ngOnInit() {
    const id = this.docId();
    if (id) {
      this.loadDoc(id);
      this.loadVersions(id);
    }
  }

  loadDoc(id: number) {
    this.loading.set(true);
    this.error.set('');
    this.docService.getById(id).subscribe({
      next: (d) => {
        this.doc.set(d);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Erro ao carregar documento.');
        this.loading.set(false);
      }
    });
  }

  startEdit() {
    const d = this.doc();
    if (!d) return;
    this.editTitle = d.title;
    this.editDescription = d.description || '';
    this.editTagsStr = (d.tags && d.tags.length) ? d.tags.join(', ') : '';
    this.editStatus = d.status;
    this.editing.set(true);
    this.error.set('');
  }

  cancelEdit() {
    this.editing.set(false);
  }

  saveEdit() {
    const id = this.docId();
    const d = this.doc();
    if (!id || !d) return;
    if (!this.editTitle.trim()) {
      this.error.set('Título é obrigatório.');
      return;
    }
    if (this.editTitle.length > 50) {
      this.error.set('Título deve ter no máximo 50 caracteres.');
      return;
    }
    this.error.set('');
    this.editSaving.set(true);
    const tags = this.editTagsStr
      ? this.editTagsStr.split(',').map(t => t.trim()).filter(t => t.length > 0 && t.length <= 25)
      : [];
    const body: DocumentRequest = {
      title: this.editTitle.trim(),
      description: this.editDescription.trim() || undefined,
      tags: tags.length ? tags : undefined,
      status: this.editStatus
    };
    this.docService.update(id, body).subscribe({
      next: () => {
        this.editSaving.set(false);
        this.editing.set(false);
        this.loadDoc(id);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Erro ao salvar.');
        this.editSaving.set(false);
      }
    });
  }

  loadVersions(id: number) {
    this.docService.listVersions(id).subscribe({
      next: (v) => this.versions.set(v),
      error: () => this.versions.set([])
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      const ext = file.name.toLowerCase().slice(-4);
      if (['.pdf', '.png', '.jpg'].some(e => ext === e) || file.name.toLowerCase().endsWith('.jpeg')) {
        this.uploadFile.set(file);
        this.uploadError.set('');
      } else {
        this.uploadError.set('Use PDF, PNG ou JPG.');
      }
    }
  }

  uploadVersion() {
    const file = this.uploadFile();
    const id = this.docId();
    if (!file || !id) return;
    this.uploadError.set('');
    this.docService.uploadVersion(id, file).subscribe({
      next: (newVersion) => {
        this.uploadFile.set(null);
        this.versions.update(list => [newVersion, ...list.filter(x => x.id !== newVersion.id)]);
        this.loadDoc(id);
      },
      error: (err) => this.uploadError.set(err?.error?.message || 'Erro no upload.')
    });
  }

  downloadCurrent() {
    const id = this.docId();
    if (!id) return;
    this.docService.downloadCurrent(id).subscribe({
      next: (result) => {
        const url = URL.createObjectURL(result.blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = result.filename;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => this.error.set(this.downloadErrorMessage(err))
    });
  }

  downloadVersion(versionId: number) {
    const id = this.docId();
    if (!id) return;
    this.docService.downloadVersion(id, versionId).subscribe({
      next: (result) => {
        const url = URL.createObjectURL(result.blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = result.filename;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => this.error.set(this.downloadErrorMessage(err))
    });
  }

  private downloadErrorMessage(err: { status?: number; error?: { message?: string } }): string {
    if (err?.status === 401) return 'Sessão expirada. Faça login novamente.';
    if (err?.status === 403) return 'Acesso negado.';
    return err?.error?.message || 'Erro ao baixar o arquivo.';
  }

  openDeleteConfirm() {
    this.showDeleteModal.set(true);
  }

  closeDeleteConfirm() {
    this.showDeleteModal.set(false);
  }

  confirmDelete() {
    const id = this.docId();
    if (!id) return;
    this.error.set('');
    this.docService.delete(id).subscribe({
      next: () => {
        this.closeDeleteConfirm();
        this.router.navigate(['/app/documents']);
      },
      error: (err) => this.error.set(err?.error?.message || 'Erro ao excluir documento.')
    });
  }
}
