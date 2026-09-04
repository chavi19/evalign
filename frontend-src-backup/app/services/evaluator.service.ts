import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Evaluator } from '../models/evaluator.model';

@Injectable({
  providedIn: 'root'
})
export class EvaluatorService {
  private readonly API_URL = 'http://localhost:8080/api/evaluators';

  private mockEvaluators: Evaluator[] = [
    { id: '1', empId: 'E1001', name: 'John Doe', vertical: 'Engineering', domain: 'Frontend', isAvailable: true },
    { id: '2', empId: 'E1002', name: 'Jane Smith', vertical: 'Engineering', domain: 'Backend', isAvailable: false, unavailableFrom: '2024-01-01', unavailableTo: '2024-12-31' },
    { id: '3', empId: 'E1003', name: 'Mike Johnson', vertical: 'Design', domain: 'UI/UX', isAvailable: true }
  ];

  constructor(private http: HttpClient) {}

  getEvaluators(filters?: any): Observable<Evaluator[]> {
    let result = [...this.mockEvaluators];
    if (filters?.vertical && filters.vertical !== 'All') {
      result = result.filter(e => e.vertical === filters.vertical);
    }
    if (filters?.domain && filters.domain !== 'All') {
      result = result.filter(e => e.domain === filters.domain);
    }
    if (filters?.availability && filters.availability !== 'All') {
      const isAvailable = filters.availability === 'Available';
      result = result.filter(e => e.isAvailable === isAvailable);
    }
    return of(result);
    // return this.http.get<Evaluator[]>(this.API_URL, { params: filters });
  }

  updateAvailability(id: string, data: { isAvailable: boolean; unavailableFrom?: string; unavailableTo?: string }): Observable<Evaluator> {
    const idx = this.mockEvaluators.findIndex(e => e.id === id);
    if(idx > -1) {
      this.mockEvaluators[idx] = { ...this.mockEvaluators[idx], ...data };
    }
    return of(this.mockEvaluators[idx]);
    // return this.http.put<Evaluator>(`${this.API_URL}/${id}/availability`, data);
  }

  uploadExcel(file: File): Observable<{ success: boolean; message: string }> {
    return of({ success: true, message: 'File uploaded successfully' });
    // const formData = new FormData();
    // formData.append('file', file);
    // return this.http.post<{ success: boolean; message: string }>(`${this.API_URL}/upload`, formData);
  }

  getVerticals(): Observable<string[]> {
    return of(['Engineering', 'Design', 'Product', 'Marketing']);
    // return this.http.get<string[]>(`${this.API_URL}/verticals`);
  }

  getDomains(): Observable<string[]> {
    return of(['Frontend', 'Backend', 'UI/UX', 'Data Science', 'Growth']);
    // return this.http.get<string[]>(`${this.API_URL}/domains`);
  }
}
