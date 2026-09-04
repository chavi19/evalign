export interface Evaluator {
  evaluatorId: number;
  empId: string;
  name: string;
  vertical: string;
  domain: string;
  availabilityStatus: string;
  unavailableFrom?: string;
  unavailableTo?: string;
  isShortlisted?: boolean;
}
