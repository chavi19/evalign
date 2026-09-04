import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Mapping } from '../models/mapping.model';
import { Evaluator } from '../models/evaluator.model';

@Injectable({
  providedIn: 'root'
})
export class MappingService {
  private readonly API_URL = 'http://localhost:8080/api';

  private mockMappings: Mapping[] = [
    { id: '1', candidateId: 'c1', candidateName: 'Alice Smith', attempts: 1, evaluatorId: '1', evaluatorName: 'John Doe', isAvailable: true, stage: 'Interim', cohortId: '1' },
    { id: '2', candidateId: 'c2', candidateName: 'Bob Jones', attempts: 1, evaluatorId: '3', evaluatorName: 'Mike Johnson', isAvailable: true, stage: 'Interim', cohortId: '1' },
    { id: '3', candidateId: 'c1', candidateName: 'Alice Smith', attempts: 1, evaluatorId: '2', evaluatorName: 'Jane Smith', previousEvaluatorId: '1', previousEvaluatorName: 'John Doe', isAvailable: false, stage: 'Final', cohortId: '1' }
  ];

  constructor(private http: HttpClient) {}

  getMappings(cohortId: string, round: 'Interim' | 'Final'): Observable<Mapping[]> {
    return of(this.mockMappings.filter(m => m.cohortId === cohortId && m.stage === round));
    // return this.http.get<Mapping[]>(`${this.API_URL}/cohorts/${cohortId}/mappings`, { params: { round } });
  }

  createMapping(data: Partial<Mapping>): Observable<Mapping> {
    const newMapping: Mapping = {
      id: Math.random().toString(36).substring(7),
      candidateId: data.candidateId || '',
      candidateName: data.candidateName || '',
      attempts: data.attempts || 1,
      evaluatorId: data.evaluatorId,
      evaluatorName: data.evaluatorName,
      previousEvaluatorId: data.previousEvaluatorId,
      previousEvaluatorName: data.previousEvaluatorName,
      isAvailable: data.isAvailable || true,
      stage: data.stage || 'Interim',
      cohortId: data.cohortId || ''
    };
    this.mockMappings.push(newMapping);
    return of(newMapping);
    // return this.http.post<Mapping>(`${this.API_URL}/mappings`, data);
  }

  autoMap(cohortId: string, round: 'Interim' | 'Final'): Observable<Mapping[]> {
    // Mock auto-map behavior
    const updated = this.mockMappings.filter(m => m.cohortId === cohortId && m.stage === round).map(m => {
      m.evaluatorId = '1';
      m.evaluatorName = 'John Doe';
      return m;
    });
    return of(updated);
    // return this.http.post<Mapping[]>(`${this.API_URL}/cohorts/${cohortId}/auto-map`, null, { params: { round } });
  }

  confirmMapping(mappingId: string): Observable<Mapping> {
    const mapping = this.mockMappings.find(m => m.id === mappingId);
    return of(mapping!);
    // return this.http.put<Mapping>(`${this.API_URL}/mappings/${mappingId}/confirm`, {});
  }

  getEligibleEvaluators(cohortId: string, candidateId: string, round: 'Interim' | 'Final'): Observable<Evaluator[]> {
    return of([
      { id: '1', empId: 'E1001', name: 'John Doe', vertical: 'Engineering', domain: 'Frontend', isAvailable: true },
      { id: '3', empId: 'E1003', name: 'Mike Johnson', vertical: 'Design', domain: 'UI/UX', isAvailable: true }
    ]);
    // return this.http.get<Evaluator[]>(`${this.API_URL}/cohorts/${cohortId}/eligible-evaluators`, { params: { candidateId, round } });
  }
}
