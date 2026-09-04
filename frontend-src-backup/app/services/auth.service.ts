import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const token = this.getToken();
    if (token) {
      // Mock decoding JWT for now
      this.currentUserSubject.next({ id: 1, email: 'admin@example.com', name: 'Batch Owner', role: 'admin' });
    }
  }

  login(email: string, password: string): Observable<any> {
    // Mock login since we might not have a real backend
    return of({ token: 'mock-jwt-token-12345' }).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        this.currentUserSubject.next({ id: 1, email, name: 'Batch Owner', role: 'admin' });
      })
    );
    // Real implementation: return this.http.post(`${this.API_URL}/login`, { email, password });
  }

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
}
