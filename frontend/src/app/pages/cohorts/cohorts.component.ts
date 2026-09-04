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
  }

  closeModal(): void {
    this.showModal = false;
    this.newCohort = {
      cohortName: '',
      batchCode: '',
      startDate: new Date().toISOString().substring(0, 10),
      status: 'ACTIVE',
      candidateCount: 0
    };
  }

  saveCohort(): void {
    if (!this.newCohort.cohortName || !this.newCohort.batchCode) return;
    this.cohortService.createCohort(this.newCohort).subscribe({
      next: () => {
        this.loadCohorts();
        this.closeModal();
      },
      error: (err) => console.error('Failed to create cohort', err)
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
      default: return 'badge-default';
    }
  }
}
