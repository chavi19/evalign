export interface Cohort {
  id: string;
  name: string;
  batchCode: string;
  candidateCount: number;
  evaluatorsMapped: number;
  evaluatorsTotal: number;
  startDate: string;
  status: 'Active' | 'Completed' | 'In Progress' | 'Not Started';
}
