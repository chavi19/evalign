export interface Mapping {
  id: string;
  candidateId: string;
  candidateName: string;
  attempts: number;
  evaluatorId?: string;
  evaluatorName?: string;
  previousEvaluatorId?: string;
  previousEvaluatorName?: string;
  isAvailable: boolean;
  mappedBy?: string;
  mappedAt?: string;
  stage: 'Interim' | 'Final';
  cohortId: string;
}
