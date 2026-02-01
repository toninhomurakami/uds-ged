import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_URL } from '../core/api';

export interface DownloadResult {
  blob: Blob;
  filename: string;
}

export type DocumentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface Document {
  id: number;
  title: string;
  description: string;
  tags: string[];
  ownerId: number;
  ownerName: string;
  status: DocumentStatus;
  createdAt: string;
  updatedAt: string;
  hasCurrentVersion: boolean;
}

export interface DocumentVersion {
  id: number;
  fileKey: string;
  uploadedAt: string;
  uploadedById: number;
  uploadedByName: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface DocumentRequest {
  title: string;
  description?: string;
  tags?: string[];
  status?: DocumentStatus;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  constructor(private http: HttpClient) {}

  list(title?: string, status?: DocumentStatus, page = 0, size = 10, sort = 'updatedAt,desc'): Observable<PageResponse<Document>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    if (title) params = params.set('title', title);
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<Document>>(`${API_URL}/documents`, { params });
  }

  getById(id: number): Observable<Document> {
    return this.http.get<Document>(`${API_URL}/documents/${id}`);
  }

  create(body: DocumentRequest): Observable<Document> {
    return this.http.post<Document>(`${API_URL}/documents`, body);
  }

  update(id: number, body: DocumentRequest): Observable<Document> {
    return this.http.put<Document>(`${API_URL}/documents/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/documents/${id}`);
  }

  publish(id: number): Observable<Document> {
    return this.http.put<Document>(`${API_URL}/documents/${id}/publish`, {});
  }

  archive(id: number): Observable<Document> {
    return this.http.put<Document>(`${API_URL}/documents/${id}/archive`, {});
  }

  listVersions(documentId: number): Observable<DocumentVersion[]> {
    return this.http.get<DocumentVersion[]>(`${API_URL}/documents/${documentId}/versions`);
  }

  uploadVersion(documentId: number, file: File): Observable<DocumentVersion> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<DocumentVersion>(`${API_URL}/documents/${documentId}/versions`, form);
  }

  downloadCurrent(documentId: number): Observable<DownloadResult> {
    return this.http.get(`${API_URL}/documents/${documentId}/download`, {
      responseType: 'blob',
      observe: 'response'
    }).pipe(
      map(res => ({
        blob: res.body!,
        filename: this.filenameFromDisposition(res.headers.get('Content-Disposition')) || `document-${documentId}.pdf`
      }))
    );
  }

  downloadVersion(documentId: number, versionId: number): Observable<DownloadResult> {
    return this.http.get(`${API_URL}/documents/${documentId}/versions/${versionId}/download`, {
      responseType: 'blob',
      observe: 'response'
    }).pipe(
      map(res => ({
        blob: res.body!,
        filename: this.filenameFromDisposition(res.headers.get('Content-Disposition')) || `document-${documentId}-v${versionId}.pdf`
      }))
    );
  }

  private filenameFromDisposition(disposition: string | null): string | null {
    if (!disposition) return null;
    const m = disposition.match(/filename="([^"]*)"/) || disposition.match(/filename\*?=\s*"?(?:UTF-8'')?([^";\s]+)"?/i);
    return m ? (m[1] || m[2] || '').trim().replace(/^["']|["']$/g, '') || null : null;
  }
}
