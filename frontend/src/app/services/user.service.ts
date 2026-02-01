import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api';

export type Role = 'ADMIN' | 'USER';

export interface User {
  id: number;
  name: string;
  username: string;
  role: Role;
}

export interface UserRequest {
  name: string;
  username: string;
  password?: string;
  role: Role;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}

  list(): Observable<User[]> {
    return this.http.get<User[]>(`${API_URL}/users`);
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${API_URL}/users/${id}`);
  }

  create(body: UserRequest): Observable<User> {
    return this.http.post<User>(`${API_URL}/users`, body);
  }

  update(id: number, body: UserRequest): Observable<User> {
    return this.http.put<User>(`${API_URL}/users/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/users/${id}`);
  }
}
