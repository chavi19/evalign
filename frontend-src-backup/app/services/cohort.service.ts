import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Cohort } from '../models/cohort.model';
import { Candidate } from '../models/candidate.model';

@Injectable({
  providedIn: 'root'
})
export class CohortService {
  private readonly API_URL = 'http://localhost:8080/api/cohorts';

  // Mock Data
  private mockCohorts: Cohort[] = [
    { id: '1', name: 'Summer Engineering 2024', batchCode: 'SE-24', candidateCount: 42, evaluatorsMapped: 38, evaluatorsTotal: 42, startDate: '2024-06-01', status: 'Active' },
    { id: '2', name: 'Fall Design 2024', batchCode: 'FD-24', candidateCount: 20, evaluatorsMapped: 20, evaluatorsTotal: 20, startDate: '2024-09-01', status: 'In Progress' },
    { id: '3', name: 'Spring Data 2024', batchCode: 'SD-24', candidateCount: 30, evaluatorsMapped: 30, evaluatorsTotal: 30, startDate: '2024-02-01', status: 'Completed' }
  ];

  constructor(private http: HttpClient) {}

  getCohorts(search?: string, status?: string): Observable<Cohort[]> {
    let filtered = [...this.mockCohorts];
    if (search) filtered = filtered.filter(c => c.name.toLowerCase().includes(search.toLowerCase()) || c.batchCode.toLowerCase().includes(search.toLowerCase()));
    if (status && status !== 'All') filtered = filtered.filter(c => c.status === status);
    return of(filtered);
    // return this.http.get<Cohort[]>(this.API_URL, { params: { search: search || '', status: status || '' } });
  }

  getCohort(id: string): Observable<Cohort> {
    return of(this.mockCohorts.find(c => c.id === id)!);
    // return this.http.get<Cohort>(`${this.API_URL}/${id}`);
  }

  createCohort(data: Partial<Cohort>): Observable<Cohort> {
    const newCohort: Cohort = {
      id: Math.random().toString(36).substring(7),
      name: data.name || '',
      batchCode: data.batchCode || '',
      candidateCount: 0,
      evaluatorsMapped: 0,
      evaluatorsTotal: 0,
      startDate: data.startDate || '',
      status: (data.status as any) || 'Not Started'
    };
    this.mockCohorts.push(newCohort);
    return of(newCohort);
    // return this.http.post<Cohort>(this.API_URL, data);
  }

  updateCohort(id: string, data: Partial<Cohort>): Observable<Cohort> {
    const idx = this.mockCohorts.findIndex(c => c.id === id);
    if(idx > -1) {
      this.mockCohorts[idx] = { ...this.mockCohorts[idx], ...data };
    }
    return of(this.mockCohorts[idx]);
    // return this.http.put<Cohort>(`${this.API_URL}/${id}`, data);
  }

  deleteCohort(id: string): Observable<void> {
    this.mockCohorts = this.mockCohorts.filter(c => c.id !== id);
    return of(undefined);
    // return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getCandidates(cohortId: string): Observable<Candidate[]> {
    return of([
      { id: '1', name: 'Alice Smith', email: 'alice@example.com', cohortId, status: 'Active' },
      { id: '2', name: 'Bob Jones', email: 'bob@example.com', cohortId, status: 'Active' }
    ]);
    // return this.http.get<Candidate[]>(`${this.API_URL}/${cohortId}/candidates`);
  }

  getShortlist(cohortId: string): Observable<any[]> {
    return of([]);
    // return this.http.get<any[]>(`${this.API_URL}/${cohortId}/shortlist`);
  }

  addToShortlist(cohortId: string, evaluatorId: string): Observable<void> {
    return of(undefined);
    // return this.http.post<void>(`${this.API_URL}/${cohortId}/shortlist/${evaluatorId}`, {});
  }

  removeFromShortlist(cohortId: string, evaluatorId: string): Observable<void> {
    return of(undefined);
    // return this.http.delete<void>(`${this.API_URL}/${cohortId}/shortlist/${evaluatorId}`);
  }
}
