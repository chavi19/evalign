import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cohort } from '../models/cohort.model';
import { Candidate } from '../models/candidate.model';
import { Evaluator } from '../models/evaluator.model';

@Injectable({
  providedIn: 'root'
})
export class CohortService {
  private readonly API_URL = 'http://localhost:8080/api/cohorts';

  constructor(private http: HttpClient) {}

  getCohorts(): Observable<Cohort[]> {
    return this.http.get<Cohort[]>(this.API_URL);
  }

  getCohort(id: number | string): Observable<Cohort> {
    return this.http.get<Cohort>(`${this.API_URL}/${id}`);
  }

  createCohort(data: Partial<Cohort>): Observable<Cohort> {
    return this.http.post<Cohort>(this.API_URL, data);
  }

  updateCohort(id: number | string, data: Partial<Cohort>): Observable<Cohort> {
    return this.http.put<Cohort>(`${this.API_URL}/${id}`, data);
  }

  deleteCohort(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getCandidates(cohortId: number | string): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(`http://localhost:8080/api/cohorts/${cohortId}/candidates`);
  }

  getShortlist(cohortId: number | string): Observable<Evaluator[]> {
    return this.http.get<Evaluator[]>(`${this.API_URL}/${cohortId}/shortlist`);
  }

  addToShortlist(cohortId: number | string, evaluatorId: number | string): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/${cohortId}/evaluators/${evaluatorId}`, {});
  }

  removeFromShortlist(cohortId: number | string, evaluatorId: number | string): Observable<any> {
    return this.http.delete<any>(`${this.API_URL}/${cohortId}/shortlist/${evaluatorId}`);
  }
}
