export interface Mapping {
  mappingId?: number;
  cohortId: number;
  cohortName?: string;
  candidateId: number;
  candidateName: string;
  evaluatorId?: number;
  evaluatorName?: string;
  round: string; // INTERIM, FINAL
  attempt: number;
  status: string; // SUGGESTED, CONFIRMED
  interimEvaluatorId?: number;
  interimEvaluatorName?: string;
  evaluatorAvailability?: string;
  mappedByName?: string;
  mappedAt?: string;
  ruleWarning?: string;
}
