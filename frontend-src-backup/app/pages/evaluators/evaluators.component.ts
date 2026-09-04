import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EvaluatorService } from '../../services/evaluator.service';
import { CohortService } from '../../services/cohort.service';
import { Evaluator } from '../../models/evaluator.model';
import { Cohort } from '../../models/cohort.model';

@Component({
  selector: 'app-evaluators',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './evaluators.component.html',
  styleUrls: ['./evaluators.component.css']
})
export class EvaluatorsComponent implements OnInit {
  evaluators: Evaluator[] = [];
  cohorts: Cohort[] = [];
  
  verticals: string[] = [];
  domains: string[] = [];
  
  filters = {
    search: '',
    vertical: 'All',
    domain: 'All',
    availability: 'All'
  };

  constructor(
    private evaluatorService: EvaluatorService,
    private cohortService: CohortService
  ) {}

  ngOnInit(): void {
    this.loadFilters();
    this.loadEvaluators();
    this.loadCohorts();
  }

  loadFilters() {
    this.evaluatorService.getVerticals().subscribe(v => this.verticals = v);
    this.evaluatorService.getDomains().subscribe(d => this.domains = d);
  }

  loadEvaluators() {
    this.evaluatorService.getEvaluators(this.filters).subscribe(res => {
      this.evaluators = res;
    });
  }

  loadCohorts() {
    this.cohortService.getCohorts('', 'Active').subscribe(res => {
      this.cohorts = res;
    });
  }

  onFilterChange() {
    this.loadEvaluators();
  }

  toggleAvailability(evaluator: Evaluator) {
    const newStatus = !evaluator.isAvailable;
    this.evaluatorService.updateAvailability(evaluator.id, { isAvailable: newStatus }).subscribe(res => {
      const idx = this.evaluators.findIndex(e => e.id === res.id);
      if(idx > -1) this.evaluators[idx] = res;
    });
  }

  onFileUpload(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.evaluatorService.uploadExcel(file).subscribe(res => {
        alert(res.message);
        this.loadEvaluators();
      });
    }
  }

  addToCohort(evaluatorId: string, event: any) {
    const cohortId = event.target.value;
    if (cohortId) {
      this.cohortService.addToShortlist(cohortId, evaluatorId).subscribe(() => {
        alert('Added to cohort successfully');
        event.target.value = ''; // Reset select
      });
    }
  }
}
