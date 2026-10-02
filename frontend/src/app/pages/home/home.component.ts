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
    availability: 'All',
    interviewFrom: '',
    interviewTo: ''
  };

  dateFilterError: string = '';
  notification: { message: string; type: 'success' | 'error' } | null = null;
  
  // Cohort Modal
  selectedCohortId: number | null = null;
  showCohortModal: boolean = false;
  selectedEvaluatorForCohort: Evaluator | null = null;

  // Status Reason Modal
  showStatusModal: boolean = false;
  selectedEvaluatorForStatus: Evaluator | null = null;
  statusModalForm = {
    statusReason: '',
    isPermanent: false,
    availabilityStatus: 'UNAVAILABLE',
    unavailableFrom: '',
    unavailableTo: ''
  };
  statusModalError: string = '';

  commonReasons: string[] = [
    'On Leave',
    'Medical Leave',
    'Client Project Deadline',
    'Moved to another city',
    'Moved to another vertical',
    'Left company',
    'No longer with company',
    'Other'
  ];

  totalEvaluatorsCount: number = 0;
  availableEvaluatorsCount: number = 0;
  activeCohortsCount: number = 0;

  loading: boolean = false;
  loadingMore: boolean = false;
  hasMore: boolean = false;
  page: number = 0;
  pageSize: number = 15;

  constructor(
    private evaluatorService: EvaluatorService,
    private cohortService: CohortService
  ) {}

  ngOnInit(): void {
    this.loadFilters();
    this.loadSummaryMetrics();
    this.loadEvaluators(true);
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

  loadSummaryMetrics(): void {
    this.evaluatorService.getSummary().subscribe({
      next: (summary) => {
        this.totalEvaluatorsCount = summary.totalEvaluators || 0;
        this.availableEvaluatorsCount = summary.availableEvaluators || 0;
      },
      error: (err) => console.error('Failed to load evaluator summary', err)
    });
  }

  loadEvaluators(reset: boolean = true): void {
    // Validate date range before sending
    if (this.filters.interviewFrom && this.filters.interviewTo) {
      if (this.filters.interviewFrom > this.filters.interviewTo) {
        this.dateFilterError = 'From Date cannot be after To Date';
        return;
      } else {
        this.dateFilterError = '';
      }
    } else {
      this.dateFilterError = '';
    }

    if (reset) {
      this.page = 0;
      this.loading = true;
      this.evaluators = [];
      this.filteredEvaluators = [];
    } else {
      this.loadingMore = true;
    }

    this.evaluatorService.getEvaluators({
      vertical: this.filters.vertical,
      domain: this.filters.domain,
      availability: this.filters.availability,
      interviewFrom: this.filters.interviewFrom || undefined,
      interviewTo: this.filters.interviewTo || undefined,
      search: this.filters.search ? this.filters.search.trim() : undefined,
      page: this.page,
      size: this.pageSize
    }).subscribe({
      next: (res) => {
        const batch = res || [];
        if (reset) {
          this.evaluators = batch;
        } else {
          this.evaluators = [...this.evaluators, ...batch];
        }
        this.filteredEvaluators = this.evaluators;
        this.hasMore = batch.length === this.pageSize;
        this.loading = false;
        this.loadingMore = false;
      },
      error: (err) => {
        console.error('Failed to load evaluators', err);
        this.showNotification(err.error?.message || 'Failed to load evaluators', 'error');
        this.loading = false;
        this.loadingMore = false;
      }
    });
  }

  loadCohorts(): void {
    this.cohortService.getCohorts().subscribe({
      next: (res) => {
        this.cohorts = res || [];
        this.activeCohortsCount = this.cohorts.filter(c => (c.status || '').toUpperCase() === 'ACTIVE').length;
        if (this.cohorts.length > 0 && !this.selectedCohortId) {
          this.selectedCohortId = this.cohorts[0].cohortId;
        }
      },
      error: (err) => console.error('Failed to load cohorts', err)
    });
  }

  viewMore(): void {
    if (!this.hasMore || this.loading || this.loadingMore) return;
    this.page++;
    this.loadEvaluators(false);
  }

  onFilterChange(): void {
    this.loadEvaluators(true);
  }

  onSearchChange(): void {
    this.loadEvaluators(true);
  }

  clearDateFilter(): void {
    this.filters.interviewFrom = '';
    this.filters.interviewTo = '';
    this.dateFilterError = '';
    this.loadEvaluators();
  }

  toggleAvailability(evaluator: Evaluator): void {
    const isNowAvailable = evaluator.availabilityStatus === 'AVAILABLE';
    const newStatus = isNowAvailable ? 'UNAVAILABLE' : 'AVAILABLE';
    const today = new Date().toISOString().substring(0, 10);
    const nextWeek = new Date(Date.now() + 5 * 86400000).toISOString().substring(0, 10);

    const updatePayload = {
      availabilityStatus: newStatus,
      unavailableFrom: newStatus === 'UNAVAILABLE' ? today : undefined,
      unavailableTo: newStatus === 'UNAVAILABLE' ? nextWeek : undefined,
      statusReason: newStatus === 'UNAVAILABLE' ? (evaluator.statusReason || 'Temporary unavailability') : undefined,
      isPermanent: newStatus === 'UNAVAILABLE' ? (evaluator.isPermanent || false) : false
    };

    this.evaluatorService.updateAvailability(evaluator.evaluatorId, updatePayload).subscribe({
      next: (updated) => {
        evaluator.availabilityStatus = updated.availabilityStatus;
        evaluator.unavailableFrom = updated.unavailableFrom;
        evaluator.unavailableTo = updated.unavailableTo;
        evaluator.statusReason = updated.statusReason;
        evaluator.isPermanent = updated.isPermanent;
        this.showNotification(`Updated ${evaluator.name} status to ${newStatus}`);
        this.loadSummaryMetrics();
      },
      error: (err) => this.showNotification(err.error?.message || 'Failed to update availability', 'error')
    });
  }

  // --- Status Reason Modal Methods ---
  openStatusModal(evaluator: Evaluator): void {
    this.selectedEvaluatorForStatus = evaluator;
    const today = new Date().toISOString().substring(0, 10);
    const nextWeek = new Date(Date.now() + 5 * 86400000).toISOString().substring(0, 10);

    this.statusModalForm = {
      statusReason: evaluator.statusReason || 'On Leave',
      isPermanent: evaluator.isPermanent || false,
      availabilityStatus: evaluator.availabilityStatus || 'UNAVAILABLE',
      unavailableFrom: evaluator.unavailableFrom ? String(evaluator.unavailableFrom) : today,
      unavailableTo: evaluator.unavailableTo ? String(evaluator.unavailableTo) : nextWeek
    };
    this.statusModalError = '';
    this.showStatusModal = true;
  }

  closeStatusModal(): void {
    this.showStatusModal = false;
    this.selectedEvaluatorForStatus = null;
    this.statusModalError = '';
  }

  selectQuickReason(reason: string): void {
    this.statusModalForm.statusReason = reason;
    if (reason === 'Left company' || reason === 'No longer with company' || reason.includes('Moved permanently')) {
      this.statusModalForm.isPermanent = true;
    }
  }

  saveStatusReason(): void {
    if (!this.selectedEvaluatorForStatus) return;

    if (!this.statusModalForm.statusReason || !this.statusModalForm.statusReason.trim()) {
      this.statusModalError = 'Please specify a status reason';
      return;
    }

    if (!this.statusModalForm.isPermanent) {
      if (!this.statusModalForm.unavailableFrom || !this.statusModalForm.unavailableTo) {
        this.statusModalError = 'Please select From Date and To Date for temporary status';
        return;
      }
      if (this.statusModalForm.unavailableFrom > this.statusModalForm.unavailableTo) {
        this.statusModalError = 'Unavailable From Date cannot be after To Date';
        return;
      }
    }

    const payload = {
      statusReason: this.statusModalForm.statusReason.trim(),
      isPermanent: this.statusModalForm.isPermanent,
      availabilityStatus: this.statusModalForm.availabilityStatus,
      unavailableFrom: this.statusModalForm.isPermanent ? undefined : this.statusModalForm.unavailableFrom,
      unavailableTo: this.statusModalForm.isPermanent ? undefined : this.statusModalForm.unavailableTo
    };

    this.evaluatorService.updateStatusReason(this.selectedEvaluatorForStatus.evaluatorId, payload).subscribe({
      next: (updated) => {
        if (this.selectedEvaluatorForStatus) {
          this.selectedEvaluatorForStatus.statusReason = updated.statusReason;
          this.selectedEvaluatorForStatus.isPermanent = updated.isPermanent;
          this.selectedEvaluatorForStatus.availabilityStatus = updated.availabilityStatus;
          this.selectedEvaluatorForStatus.unavailableFrom = updated.unavailableFrom;
          this.selectedEvaluatorForStatus.unavailableTo = updated.unavailableTo;
        }
        this.showNotification(`Saved status reason for ${updated.name}`);
        this.closeStatusModal();
        this.loadEvaluators();
      },
      error: (err) => {
        this.statusModalError = err.error?.message || 'Failed to save status reason';
      }
    });
  }

  // --- Excel Upload & Download ---
  onFileUpload(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.evaluatorService.uploadExcel(file).subscribe({
        next: (res) => {
          this.showNotification(res.message || 'Evaluators uploaded and master roster updated successfully!');
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

  downloadMasterExcel(): void {
    this.evaluatorService.exportExcel().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'master_evaluators.xlsx';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.showNotification('Master Evaluator roster downloaded successfully');
      },
      error: (err) => {
        this.showNotification('Failed to download master Excel roster', 'error');
      }
    });
  }

  // --- Cohort Modal Methods ---
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
