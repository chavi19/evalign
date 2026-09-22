import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const userJson = localStorage.getItem('user');
    if (userJson) {
      try {
        this.currentUserSubject.next(JSON.parse(userJson));
      } catch (e) {
        this.logout();
      }
    }
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/login`, { email, password }).pipe(
      tap(res => {
        if (res && res.token) {
          localStorage.setItem('token', res.token);
          const user: User = {
            userId: res.userId,
            name: res.name || 'Batch Owner',
            email: res.email || email,
            role: res.role || 'POC'
          };
          localStorage.setItem('user', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
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
