import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { MappingService } from '../../services/mapping.service';
import { CohortService } from '../../services/cohort.service';
import { Mapping } from '../../models/mapping.model';
import { Cohort } from '../../models/cohort.model';
import { Candidate } from '../../models/candidate.model';
import { Evaluator } from '../../models/evaluator.model';

interface MappingRow {
  mappingId?: number;
  candidateId: number;
  candidateName: string;
  attempt: number;
  attemptLabel: string;
  evaluatorId?: number;
  evaluatorName?: string;
  interimEvaluatorName?: string;
  interimEvaluatorId?: number;
  status: string; // 'SUGGESTED' | 'CONFIRMED' | 'UNASSIGNED'
  ruleWarning?: string | null;
  evaluatorAvailability?: string;
  isEditing?: boolean;
}

@Component({
  selector: 'app-mapping',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './mapping.component.html',
  styleUrls: ['./mapping.component.css']
})
export class MappingComponent implements OnInit {
  cohorts: Cohort[] = [];
  selectedCohortId: number | null = null;
  cohortId: number = 0;
  cohort: Cohort | null = null;
  activeTab: 'INTERIM' | 'FINAL' = 'INTERIM';
  
  candidates: Candidate[] = [];
  shortlistedEvaluators: Evaluator[] = [];
  mappings: Mapping[] = [];
  tableRows: MappingRow[] = [];
  visibleRows: MappingRow[] = [];
  displayLimit: number = 20;
  hasMoreRows: boolean = false;
  
  notification: { message: string; type: 'success' | 'error' | 'warning' } | null = null;
  loading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private mappingService: MappingService,
    private cohortService: CohortService
  ) {}

  ngOnInit(): void {
    this.loadCohortsList();
  }

  showNotification(message: string, type: 'success' | 'error' | 'warning' = 'success'): void {
    this.notification = { message, type };
    setTimeout(() => {
      this.notification = null;
    }, 4500);
  }

  loadCohortsList(): void {
    this.cohortService.getCohorts().subscribe({
      next: (cohortsList) => {
        this.cohorts = cohortsList || [];
        this.route.paramMap.subscribe(params => {
          const idParam = params.get('cohortId');
          if (idParam) {
            const parsedId = Number(idParam);
            const exists = this.cohorts.some(c => c.cohortId === parsedId);
            if (exists) {
              this.selectedCohortId = parsedId;
              this.cohortId = parsedId;
              this.loadCohortAllData(this.cohortId);
            } else {
              this.selectedCohortId = null;
              this.cohortId = 0;
              this.cohort = null;
              this.candidates = [];
              this.shortlistedEvaluators = [];
              this.mappings = [];
              this.tableRows = [];
              this.showNotification('Selected cohort does not exist. Please select a valid cohort.', 'warning');
            }
          } else {
            this.selectedCohortId = null;
            this.cohortId = 0;
            this.cohort = null;
            this.candidates = [];
            this.shortlistedEvaluators = [];
            this.mappings = [];
            this.tableRows = [];
          }
        });
      },
      error: (err) => {
        console.error('Failed to load cohorts', err);
        this.showNotification('Failed to load cohorts list', 'error');
      }
    });
  }

  onCohortSelected(cohortId: any): void {
    if (!cohortId) {
      this.selectedCohortId = null;
      this.cohortId = 0;
      this.cohort = null;
      this.candidates = [];
      this.shortlistedEvaluators = [];
      this.mappings = [];
      this.tableRows = [];
      this.router.navigate(['/mapping'], { replaceUrl: true });
      return;
    }
    const numId = Number(cohortId);
    this.selectedCohortId = numId;
    this.cohortId = numId;
    this.router.navigate(['/mapping', numId], { replaceUrl: true });
    this.loadCohortAllData(numId);
  }

  loadCohortAllData(cohortId: number): void {
    this.loading = true;
    forkJoin({
      cohort: this.cohortService.getCohort(cohortId),
      shortlist: this.cohortService.getShortlist(cohortId),
      candidates: this.cohortService.getCandidates(cohortId),
      mappings: this.mappingService.getMappings(cohortId, this.activeTab)
    }).subscribe({
      next: (res) => {
        this.cohort = res.cohort;
        this.shortlistedEvaluators = res.shortlist || [];
        this.candidates = res.candidates || [];
        this.mappings = res.mappings || [];
        this.buildTableRows();
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        console.error('Failed to load cohort details', err);
        this.showNotification('Failed to load cohort mapping data', 'error');
      }
    });
  }

  setTab(tab: 'INTERIM' | 'FINAL'): void {
    if (this.activeTab === tab) return;
    this.activeTab = tab;
    if (this.cohortId) {
      this.loading = true;
      this.mappingService.getMappings(this.cohortId, this.activeTab).subscribe({
        next: (maps) => {
          this.mappings = maps || [];
          this.buildTableRows();
          this.loading = false;
        },
        error: (err) => {
          this.loading = false;
          console.error('Failed to load mappings', err);
        }
      });
    }
  }

  buildTableRows(): void {
    const rows: MappingRow[] = [];

    // Map candidate IDs to their mappings in the current round
    const candMap = new Map<number, Mapping[]>();
    for (const m of this.mappings) {
      if (!candMap.has(m.candidateId)) {
        candMap.set(m.candidateId, []);
      }
      candMap.get(m.candidateId)!.push(m);
    }

    // For candidates with mappings
    for (const cand of this.candidates) {
      const existing = candMap.get(cand.candidateId);
      if (existing && existing.length > 0) {
        for (const m of existing) {
          const attemptText = m.attempt > 1 ? `Attempt ${m.attempt} (retake)` : `Attempt ${m.attempt}`;
          rows.push({
            mappingId: m.mappingId,
            candidateId: cand.candidateId,
            candidateName: cand.candidateName,
            attempt: m.attempt,
            attemptLabel: attemptText,
            evaluatorId: m.evaluatorId,
            evaluatorName: m.evaluatorName,
            interimEvaluatorName: m.interimEvaluatorName,
            interimEvaluatorId: m.interimEvaluatorId,
            status: m.status,
            ruleWarning: m.ruleWarning,
            evaluatorAvailability: m.evaluatorAvailability,
            isEditing: false
          });
        }
      } else {
        // Candidate has no mapping for this round yet
        rows.push({
          candidateId: cand.candidateId,
          candidateName: cand.candidateName,
          attempt: 1,
          attemptLabel: 'Attempt 1',
          status: 'UNASSIGNED',
          isEditing: true
        });
      }
    }

    this.tableRows = rows;
    this.displayLimit = 20;
    this.updateVisibleRows();
  }

  updateVisibleRows(): void {
    this.visibleRows = this.tableRows.slice(0, this.displayLimit);
    this.hasMoreRows = this.tableRows.length > this.displayLimit;
  }

  viewMoreRows(): void {
    this.displayLimit += 20;
    this.updateVisibleRows();
  }

  autoMap(): void {
    if (!this.cohortId || this.loading) return;
    this.loading = true;
    this.mappingService.autoMap(this.cohortId, this.activeTab).subscribe({
      next: (maps) => {
        this.mappings = maps || [];
        this.buildTableRows();
        this.loading = false;
        this.showNotification('Evaluators successfully auto-mapped for ' + (this.activeTab === 'INTERIM' ? 'Interim' : 'Final') + ' round!');
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message || 'Auto-mapping failed';
        this.showNotification(msg, 'error');
      }
    });
  }

  onEvaluatorSelected(row: MappingRow, evalId: any): void {
    const selectedId = Number(evalId);
    if (!selectedId || this.loading) return;
    
    const chosenEval = this.shortlistedEvaluators.find(e => e.evaluatorId === selectedId);
    if (chosenEval) {
      row.evaluatorId = chosenEval.evaluatorId;
      row.evaluatorName = chosenEval.name;
    }

    this.loading = true;
    this.mappingService.createMapping({
      cohortId: this.cohortId,
      candidateId: row.candidateId,
      evaluatorId: selectedId,
      round: this.activeTab,
      attempt: row.attempt
    }).subscribe({
      next: (res) => {
        row.mappingId = res.mappingId;
        row.status = res.status;
        row.ruleWarning = res.ruleWarning;
        row.evaluatorAvailability = res.evaluatorAvailability;
        row.interimEvaluatorName = res.interimEvaluatorName;
        row.interimEvaluatorId = res.interimEvaluatorId;
        row.isEditing = false;
        this.loading = false;
        if (res.ruleWarning) {
          this.showNotification(res.ruleWarning, 'warning');
        } else {
          this.showNotification(`Assigned ${row.evaluatorName} to ${row.candidateName}`);
        }
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message || 'Failed to assign evaluator';
        row.ruleWarning = msg;
        this.showNotification(msg, 'error');
      }
    });
  }

  confirmMapping(row: MappingRow): void {
    if (!row.mappingId || this.loading) {
      if (!row.mappingId) this.showNotification('Please select an evaluator first', 'warning');
      return;
    }

    if (row.ruleWarning) {
      this.showNotification(`Cannot confirm mapping: ${row.ruleWarning}`, 'error');
      return;
    }

    this.loading = true;
    this.mappingService.confirmMapping(row.mappingId).subscribe({
      next: (res) => {
        row.status = 'CONFIRMED';
        row.ruleWarning = null;
        row.isEditing = false;
        this.loading = false;
        this.showNotification(`Mapping confirmed for ${row.candidateName}!`);
      },
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message || 'Failed to confirm mapping';
        row.ruleWarning = msg;
        this.showNotification(msg, 'error');
      }
    });
  }

  enableReassign(row: MappingRow): void {
    row.isEditing = true;
  }

  cancelReassign(row: MappingRow): void {
    row.isEditing = false;
  }
}

