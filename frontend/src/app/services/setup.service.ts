import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { timeout, catchError } from 'rxjs/operators';
import { API_URL } from '../core/api';

export interface SetupStatus {
  needsSetup: boolean;
}

export interface InitialAdminRequest {
  name: string;
  username: string;
  password: string;
}

const STATUS_TIMEOUT_MS = 8000;

@Injectable({ providedIn: 'root' })
export class SetupService {
  constructor(private http: HttpClient) {}

  getStatus(): Observable<SetupStatus> {
    return this.http.get<SetupStatus>(`${API_URL}/setup/status`).pipe(
      timeout(STATUS_TIMEOUT_MS),
      catchError((err) => throwError(() => err))
    );
  }

  createInitialAdmin(body: InitialAdminRequest): Observable<unknown> {
    return this.http.post(`${API_URL}/setup/initial-admin`, body);
  }
}
