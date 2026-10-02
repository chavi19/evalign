import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mapping } from '../models/mapping.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly API_URL = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  getReports(paramsObj?: {
    page?: number;
    size?: number;
    search?: string;
    cohortName?: string;
    stage?: string;
    status?: string;
  }): Observable<Mapping[]> {
    let params = new HttpParams();
    if (paramsObj) {
      if (paramsObj.page !== undefined && paramsObj.page !== null) params = params.set('page', paramsObj.page.toString());
      if (paramsObj.size !== undefined && paramsObj.size !== null) params = params.set('size', paramsObj.size.toString());
      if (paramsObj.search) params = params.set('search', paramsObj.search);
      if (paramsObj.cohortName && paramsObj.cohortName !== 'All') params = params.set('cohortName', paramsObj.cohortName);
      if (paramsObj.stage && paramsObj.stage !== 'All') params = params.set('stage', paramsObj.stage);
      if (paramsObj.status && paramsObj.status !== 'All') params = params.set('status', paramsObj.status);
    }
    return this.http.get<Mapping[]>(`${this.API_URL}/mappings`, { params });
  }
}
