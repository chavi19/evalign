import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EvaluatorService } from '../../services/evaluator.service';
import { CohortService } from '../../services/cohort.service';
import { Evaluator } from '../../models/evaluator.model';
import { Cohort } from '../../models/cohort.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  evaluators: Evaluator[] = [];
  filteredEvaluators: Evaluator[] = [];
  cohorts: Cohort[] = [];
  
  verticals: string[] = [];
  domains: string[] = [];
  
  filters = {
    search: '',
    vertical: 'All',
    domain: 'All',
    availability: 'All'
  };

  notification: { message: string; type: 'success' | 'error' } | null = null;
  selectedCohortId: number | null = null;
  showCohortModal: boolean = false;
  selectedEvaluatorForCohort: Evaluator | null = null;

  constructor(
    private evaluatorService: EvaluatorService,
    private cohortService: CohortService
  ) {}

  ngOnInit(): void {
    this.loadFilters();
    this.loadEvaluators();
    this.loadCohorts();
  }

  showNotification(message: string, type: 'success' | 'error' = 'success'): void {
    this.notification = { message, type };
    setTimeout(() => {
      this.notification = null;
    }, 4000);
  }

  loadFilters(): void {
    this.evaluatorService.getVerticals().subscribe(v => this.verticals = v || []);
    this.evaluatorService.getDomains().subscribe(d => this.domains = d || []);
  }

  loadEvaluators(): void {
    this.evaluatorService.getEvaluators().subscribe({
      next: (res) => {
        this.evaluators = res || [];
        this.applyFilters();
      },
      error: (err) => console.error('Failed to load evaluators', err)
    });
  }

  loadCohorts(): void {
    this.cohortService.getCohorts().subscribe({
      next: (res) => {
        this.cohorts = res || [];
        if (this.cohorts.length > 0 && !this.selectedCohortId) {
          this.selectedCohortId = this.cohorts[0].cohortId;
        }
      },
      error: (err) => console.error('Failed to load cohorts', err)
    });
  }

  get totalEvaluatorsCount(): number {
    return this.evaluators.length;
  }

  get availableEvaluatorsCount(): number {
    return this.evaluators.filter(e => e.availabilityStatus === 'AVAILABLE').length;
  }

  get activeCohortsCount(): number {
    return this.cohorts.filter(c => c.status === 'ACTIVE').length;
  }

  applyFilters(): void {
    let result = [...this.evaluators];

    if (this.filters.search) {
      const q = this.filters.search.toLowerCase();
      result = result.filter(e => 
        (e.name && e.name.toLowerCase().includes(q)) ||
        (e.empId && e.empId.toLowerCase().includes(q))
      );
    }

    if (this.filters.vertical && this.filters.vertical !== 'All') {
      result = result.filter(e => e.vertical === this.filters.vertical);
    }

    if (this.filters.domain && this.filters.domain !== 'All') {
      result = result.filter(e => e.domain === this.filters.domain);
    }

    if (this.filters.availability && this.filters.availability !== 'All') {
      result = result.filter(e => e.availabilityStatus && e.availabilityStatus.toUpperCase() === this.filters.availability.toUpperCase());
    }

    this.filteredEvaluators = result;
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  toggleAvailability(evaluator: Evaluator): void {
    const isNowAvailable = evaluator.availabilityStatus === 'AVAILABLE';
    const newStatus = isNowAvailable ? 'UNAVAILABLE' : 'AVAILABLE';
    const today = new Date().toISOString().substring(0, 10);
    const nextWeek = new Date(Date.now() + 5 * 86400000).toISOString().substring(0, 10);

    const updatePayload = {
      availabilityStatus: newStatus,
      unavailableFrom: newStatus === 'UNAVAILABLE' ? today : undefined,
      unavailableTo: newStatus === 'UNAVAILABLE' ? nextWeek : undefined
    };

    this.evaluatorService.updateAvailability(evaluator.evaluatorId, updatePayload).subscribe({
      next: (updated) => {
        evaluator.availabilityStatus = updated.availabilityStatus;
        evaluator.unavailableFrom = updated.unavailableFrom;
        evaluator.unavailableTo = updated.unavailableTo;
        this.showNotification(`Updated ${evaluator.name} status to ${newStatus}`);
        this.applyFilters();
      },
      error: (err) => this.showNotification(err.error?.message || 'Failed to update availability', 'error')
    });
  }

  onFileUpload(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.evaluatorService.uploadExcel(file).subscribe({
        next: (res) => {
          this.showNotification(res.message || 'Evaluators uploaded successfully!');
          this.loadEvaluators();
          this.loadFilters();
        },
        error: (err) => {
          this.showNotification(err.error?.message || 'Failed to upload Excel file', 'error');
        }
      });
      event.target.value = '';
    }
  }

  openCohortModal(evaluator: Evaluator): void {
    this.selectedEvaluatorForCohort = evaluator;
    this.showCohortModal = true;
  }

  closeCohortModal(): void {
    this.showCohortModal = false;
    this.selectedEvaluatorForCohort = null;
  }

  assignToCohort(): void {
    if (!this.selectedEvaluatorForCohort || !this.selectedCohortId) return;
    const evalId = this.selectedEvaluatorForCohort.evaluatorId;
    const cohortId = this.selectedCohortId;
    const cohortName = this.cohorts.find(c => c.cohortId === cohortId)?.cohortName || 'cohort';

    this.cohortService.addToShortlist(cohortId, evalId).subscribe({
      next: () => {
        this.showNotification(`Added ${this.selectedEvaluatorForCohort?.name} to ${cohortName}`);
        this.closeCohortModal();
      },
      error: (err) => {
        this.showNotification(err.error?.message || 'Evaluator is already in this cohort or cannot be added', 'error');
        this.closeCohortModal();
      }
    });
  }
}
