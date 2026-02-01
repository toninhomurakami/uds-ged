import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, catchError, of } from 'rxjs';
import { API_URL } from '../core/api';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  role: 'ADMIN' | 'USER';
}

const TOKEN_KEY = 'ged_token';
const USER_KEY = 'ged_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenSignal = signal<string | null>(this.getStoredToken());
  private userSignal = signal<LoginResponse | null>(this.getStoredUser());

  token = this.tokenSignal.asReadonly();
  user = this.userSignal.asReadonly();
  isLoggedIn = computed(() => !!this.tokenSignal());
  isAdmin = computed(() => this.userSignal()?.role === 'ADMIN');

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, credentials).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(USER_KEY, JSON.stringify({ username: res.username, role: res.role }));
        this.tokenSignal.set(res.token);
        this.userSignal.set(res);
      }),
      catchError((err) => {
        return of(null);
      })
    );
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.tokenSignal.set(null);
    this.userSignal.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  private getStoredToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private getStoredUser(): LoginResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as LoginResponse;
    } catch {
      return null;
    }
  }
}
