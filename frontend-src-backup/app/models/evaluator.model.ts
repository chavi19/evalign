export interface Evaluator {
  id: string;
  empId: string;
  name: string;
  vertical: string;
  domain: string;
  isAvailable: boolean;
  unavailableFrom?: string;
  unavailableTo?: string;
}
