import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mapping } from '../models/mapping.model';

@Injectable({
  providedIn: 'root'
})
export class MappingService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getMappings(cohortId: number | string, round: 'INTERIM' | 'FINAL'): Observable<Mapping[]> {
    const params = new HttpParams().set('round', round);
    return this.http.get<Mapping[]>(`${this.API_URL}/cohorts/${cohortId}/mappings`, { params });
  }

  createMapping(data: { cohortId: number; candidateId: number; evaluatorId: number; round: string; attempt?: number }): Observable<Mapping> {
    return this.http.post<Mapping>(`${this.API_URL}/mappings`, data);
  }

  autoMap(cohortId: number | string, round: 'INTERIM' | 'FINAL'): Observable<Mapping[]> {
    return this.http.post<Mapping[]>(`${this.API_URL}/cohorts/${cohortId}/auto-map`, { cohortId: Number(cohortId), round });
  }

  confirmMapping(mappingId: number | string): Observable<Mapping> {
    return this.http.post<Mapping>(`${this.API_URL}/mappings/${mappingId}/confirm`, {});
  }
}
