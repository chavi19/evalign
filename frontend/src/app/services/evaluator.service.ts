import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Evaluator } from '../models/evaluator.model';

@Injectable({
  providedIn: 'root'
})
export class EvaluatorService {
  private readonly API_URL = 'http://localhost:8080/api/evaluators';

  constructor(private http: HttpClient) {}

  getEvaluators(filters?: { vertical?: string; domain?: string; availability?: string }): Observable<Evaluator[]> {
    let params = new HttpParams();
    if (filters?.vertical && filters.vertical !== 'All') {
      params = params.set('vertical', filters.vertical);
    }
    if (filters?.domain && filters.domain !== 'All') {
      params = params.set('domain', filters.domain);
    }
    if (filters?.availability && filters.availability !== 'All') {
      params = params.set('availability', filters.availability);
    }
    return this.http.get<Evaluator[]>(this.API_URL, { params });
  }

  updateAvailability(id: number | string, data: { availabilityStatus: string; unavailableFrom?: string; unavailableTo?: string }): Observable<Evaluator> {
    return this.http.put<Evaluator>(`${this.API_URL}/${id}/availability`, data);
  }

  uploadExcel(file: File): Observable<{ success: boolean; message: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ success: boolean; message: string }>(`${this.API_URL}/upload`, formData);
  }

  getVerticals(): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/verticals`);
  }

  getDomains(): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/domains`);
  }
}
