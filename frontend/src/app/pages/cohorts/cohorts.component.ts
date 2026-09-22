import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CohortService } from '../../services/cohort.service';
import { Cohort } from '../../models/cohort.model';

@Component({
  selector: 'app-cohorts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cohorts.component.html',
  styleUrls: ['./cohorts.component.css']
})
export class CohortsComponent implements OnInit {
  cohorts: Cohort[] = [];
  filteredCohorts: Cohort[] = [];
  searchTerm: string = '';
  statusFilter: string = 'All';
  showModal: boolean = false;
  
  notification: { message: string; type: 'success' | 'error' } | null = null;
  modalError: string = '';
  savingCohort: boolean = false;
  selectedCandidateFile: File | null = null;
  candidateFileName: string = '';

  newCohort: Partial<Cohort> = {
    cohortName: '',
    batchCode: '',
    startDate: new Date().toISOString().substring(0, 10),
    status: 'ACTIVE',
    candidateCount: 0
  };

  constructor(private cohortService: CohortService, private router: Router) {}

  ngOnInit(): void {
    this.loadCohorts();
  }

  showNotification(message: string, type: 'success' | 'error' = 'success'): void {
    this.notification = { message, type };
    setTimeout(() => {
      this.notification = null;
    }, 4500);
  }

  loadCohorts(): void {
    this.cohortService.getCohorts().subscribe({
      next: (res) => {
        this.cohorts = res || [];
        this.applyFilter();
      },
      error: (err) => console.error('Failed to load cohorts', err)
    });
  }

  applyFilter(): void {
    let result = [...this.cohorts];
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(c => 
        (c.cohortName && c.cohortName.toLowerCase().includes(term)) ||
        (c.batchCode && c.batchCode.toLowerCase().includes(term))
      );
    }
    if (this.statusFilter && this.statusFilter !== 'All') {
      result = result.filter(c => c.status && c.status.toUpperCase() === this.statusFilter.toUpperCase());
    }
    this.filteredCohorts = result;
  }

  openMapping(cohortId: number): void {
    this.router.navigate(['/mapping', cohortId]);
  }

  openNewCohortModal(): void {
    this.showModal = true;
    this.modalError = '';
    this.selectedCandidateFile = null;
    this.candidateFileName = '';
  }

  closeModal(): void {
    this.showModal = false;
    this.modalError = '';
    this.selectedCandidateFile = null;
    this.candidateFileName = '';
    this.newCohort = {
      cohortName: '',
      batchCode: '',
      startDate: new Date().toISOString().substring(0, 10),
      status: 'ACTIVE',
      candidateCount: 0
    };
  }

  onCandidateFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedCandidateFile = file;
      this.candidateFileName = file.name;
    }
  }

  removeSelectedFile(): void {
    this.selectedCandidateFile = null;
    this.candidateFileName = '';
  }

  saveCohort(): void {
    if (!this.newCohort.cohortName || !this.newCohort.batchCode) {
      this.modalError = 'Cohort name and batch code are required';
      return;
    }

    this.savingCohort = true;
    this.modalError = '';

    this.cohortService.createCohort(this.newCohort).subscribe({
      next: (createdCohort) => {
        if (this.selectedCandidateFile && createdCohort.cohortId) {
          // Upload Candidate Excel file
          this.cohortService.uploadCandidateExcel(createdCohort.cohortId, this.selectedCandidateFile).subscribe({
            next: (uploadRes) => {
              this.savingCohort = false;
              this.showNotification(`Cohort '${createdCohort.cohortName}' created and candidates imported successfully!`);
              this.loadCohorts();
              this.closeModal();
            },
            error: (uploadErr) => {
              this.savingCohort = false;
              this.showNotification(`Cohort created, but candidate import had an issue: ${uploadErr.error?.message || 'Invalid file'}`, 'error');
              this.loadCohorts();
              this.closeModal();
            }
          });
        } else {
          this.savingCohort = false;
          this.showNotification(`Cohort '${createdCohort.cohortName}' created successfully!`);
          this.loadCohorts();
          this.closeModal();
        }
      },
      error: (err) => {
        this.savingCohort = false;
        this.modalError = err.error?.message || 'Failed to create cohort batch';
      }
    });
  }

  getStatusClass(status: string): string {
    const s = (status || '').toUpperCase();
    switch(s) {
      case 'ACTIVE': return 'badge-green';
      case 'COMPLETED': return 'badge-blue';
      case 'IN_PROGRESS':
      case 'MAPPING IN PROGRESS':
      case 'IN PROGRESS': return 'badge-orange';
      case 'NOT_STARTED':
      case 'NOT STARTED': return 'badge-red';
      default: return 'badge-blue';
    }
  }
}
