import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MappingService } from '../../services/mapping.service';
import { CohortService } from '../../services/cohort.service';
import { EvaluatorService } from '../../services/evaluator.service';
import { Mapping } from '../../models/mapping.model';
import { Cohort } from '../../models/cohort.model';
import { Evaluator } from '../../models/evaluator.model';

@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mapping.component.html',
  styleUrls: ['./mapping.component.css']
})
export class MappingComponent implements OnInit {
  cohortId: string = '';
  cohort: Cohort | null = null;
  activeTab: 'Interim' | 'Final' = 'Interim';
  mappings: Mapping[] = [];
  availableEvaluators: Evaluator[] = []; // Simple cache for dropdowns

  constructor(
    private route: ActivatedRoute,
    private mappingService: MappingService,
    private cohortService: CohortService,
    private evaluatorService: EvaluatorService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.cohortId = params.get('cohortId') || '';
      if (this.cohortId) {
        this.loadCohortData();
        this.loadMappings();
        this.loadEligibleEvaluators();
      }
    });
  }

  loadCohortData() {
    this.cohortService.getCohort(this.cohortId).subscribe(res => this.cohort = res);
  }

  loadMappings() {
    this.mappingService.getMappings(this.cohortId, this.activeTab).subscribe(res => {
      this.mappings = res;
    });
  }

  loadEligibleEvaluators() {
    this.evaluatorService.getEvaluators({ availability: 'Available' }).subscribe(res => {
      this.availableEvaluators = res;
    });
  }

  setTab(tab: 'Interim' | 'Final') {
    this.activeTab = tab;
    this.loadMappings();
  }

  autoMap() {
    this.mappingService.autoMap(this.cohortId, this.activeTab).subscribe(res => {
      this.mappings = res;
      alert('Evaluators auto-mapped successfully');
    });
  }

  confirmMapping(mapping: Mapping) {
    if (!mapping.evaluatorId) {
      alert('Please select an evaluator first');
      return;
    }
    this.mappingService.confirmMapping(mapping.id).subscribe(() => {
      alert('Mapping confirmed');
      this.loadMappings();
    });
  }

  reassign(mapping: Mapping) {
    mapping.evaluatorId = undefined;
    mapping.evaluatorName = undefined;
  }

  getValidationMessage(mapping: Mapping): string {
    if (!mapping.evaluatorId) return '';
    if (this.activeTab === 'Final' && mapping.evaluatorId === mapping.previousEvaluatorId) {
      return 'Blocked: same as Interim';
    }
    return 'Available';
  }

  getValidationClass(mapping: Mapping): string {
    const msg = this.getValidationMessage(mapping);
    if (msg.includes('Blocked') || msg.includes('required')) return 'text-red';
    return 'text-green';
  }
}
