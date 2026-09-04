export interface Cohort {
  cohortId: number;
  cohortName: string;
  batchCode: string;
  candidateCount: number;
  evaluatorsMapped?: number;
  startDate: string;
  status: string;
}
