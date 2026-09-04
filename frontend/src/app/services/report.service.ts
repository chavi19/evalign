import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mapping } from '../models/mapping.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly API_URL = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  getReports(): Observable<Mapping[]> {
    return this.http.get<Mapping[]>(`${this.API_URL}/mappings`);
  }
}
