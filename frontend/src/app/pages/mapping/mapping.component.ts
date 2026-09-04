import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
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
  cohortId: number = 0;
  cohort: Cohort | null = null;
  activeTab: 'INTERIM' | 'FINAL' = 'INTERIM';
  
  candidates: Candidate[] = [];
  shortlistedEvaluators: Evaluator[] = [];
  mappings: Mapping[] = [];
  tableRows: MappingRow[] = [];
  
  notification: { message: string; type: 'success' | 'error' | 'warning' } | null = null;
  loading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private mappingService: MappingService,
    private cohortService: CohortService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('cohortId');
      if (idParam) {
        this.cohortId = Number(idParam);
        this.loadCohortData();
        this.loadShortlist();
        this.loadCandidatesAndMappings();
      }
    });
  }

  showNotification(message: string, type: 'success' | 'error' | 'warning' = 'success'): void {
    this.notification = { message, type };
    setTimeout(() => {
      this.notification = null;
    }, 4500);
  }

  loadCohortData(): void {
    this.cohortService.getCohort(this.cohortId).subscribe({
      next: (res) => this.cohort = res,
      error: (err) => console.error('Failed to load cohort', err)
    });
  }

  loadShortlist(): void {
    this.cohortService.getShortlist(this.cohortId).subscribe({
      next: (res) => this.shortlistedEvaluators = res || [],
      error: (err) => console.error('Failed to load shortlist', err)
    });
  }

  loadCandidatesAndMappings(): void {
    this.loading = true;
    this.cohortService.getCandidates(this.cohortId).subscribe({
      next: (cands) => {
        this.candidates = cands || [];
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
      },
      error: (err) => {
        this.loading = false;
        console.error('Failed to load candidates', err);
      }
    });
  }

  setTab(tab: 'INTERIM' | 'FINAL'): void {
    this.activeTab = tab;
    this.loadCandidatesAndMappings();
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
  }

  autoMap(): void {
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
    if (!selectedId) return;
    
    const chosenEval = this.shortlistedEvaluators.find(e => e.evaluatorId === selectedId);
    if (chosenEval) {
      row.evaluatorId = chosenEval.evaluatorId;
      row.evaluatorName = chosenEval.name;
    }

    // Save manual mapping
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
        if (res.ruleWarning) {
          this.showNotification(res.ruleWarning, 'warning');
        } else {
          this.showNotification(`Assigned ${row.evaluatorName} to ${row.candidateName}`);
        }
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to assign evaluator';
        row.ruleWarning = msg;
        this.showNotification(msg, 'error');
      }
    });
  }

  confirmMapping(row: MappingRow): void {
    if (!row.mappingId) {
      this.showNotification('Please select an evaluator first', 'warning');
      return;
    }

    if (row.ruleWarning) {
      this.showNotification(`Cannot confirm mapping: ${row.ruleWarning}`, 'error');
      return;
    }

    this.mappingService.confirmMapping(row.mappingId).subscribe({
      next: (res) => {
        row.status = 'CONFIRMED';
        row.ruleWarning = null;
        this.showNotification(`Mapping confirmed for ${row.candidateName}!`);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to confirm mapping';
        row.ruleWarning = msg;
        this.showNotification(msg, 'error');
      }
    });
  }

  enableReassign(row: MappingRow): void {
    row.isEditing = true;
  }
}
