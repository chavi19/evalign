import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly API_URL = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  getMappingReport(filters?: any): Observable<any[]> {
    return of([
      { candidate: 'Alice Smith', evaluator: 'John Doe', cohort: 'Summer Engineering 2024', stage: 'Interim', attempt: 1, mappedBy: 'Admin', mappedAt: '2024-05-01' },
      { candidate: 'Bob Jones', evaluator: 'Jane Smith', cohort: 'Fall Design 2024', stage: 'Final', attempt: 1, mappedBy: 'System (Auto)', mappedAt: '2024-08-15' }
    ]);
    // return this.http.get<any[]>(`${this.API_URL}/mappings`, { params: filters });
  }
}
