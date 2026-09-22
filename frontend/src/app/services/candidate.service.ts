import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Candidate } from '../models/candidate.model';

@Injectable({
  providedIn: 'root'
})
export class CandidateService {
  private readonly API_URL = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getCandidatesByCohort(cohortId: number | string): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(`${this.API_URL}/cohorts/${cohortId}/candidates`);
  }

  createCandidate(candidate: Partial<Candidate>): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.API_URL}/candidates`, candidate);
  }

  updateCandidate(id: number | string, candidate: Partial<Candidate>): Observable<Candidate> {
    return this.http.put<Candidate>(`${this.API_URL}/candidates/${id}`, candidate);
  }

  deleteCandidate(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/candidates/${id}`);
  }

  uploadCandidatesFromExcel(cohortId: number | string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.API_URL}/cohorts/${cohortId}/candidates/upload`, formData);
  }
}
