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
  searchTerm: string = '';
  statusFilter: string = 'All';
  showModal: boolean = false;
  
  newCohort: Partial<Cohort> = {
    name: '',
    batchCode: '',
    startDate: '',
    status: 'Not Started'
  };

  constructor(private cohortService: CohortService, private router: Router) {}

  ngOnInit(): void {
    this.loadCohorts();
  }

  loadCohorts() {
    this.cohortService.getCohorts(this.searchTerm, this.statusFilter).subscribe(res => {
      this.cohorts = res;
    });
  }

  onSearch() {
    this.loadCohorts();
  }

  openMapping(cohortId: string) {
    this.router.navigate(['/mapping', cohortId]);
  }

  openNewCohortModal() {
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.newCohort = { name: '', batchCode: '', startDate: '', status: 'Not Started' };
  }

  saveCohort() {
    this.cohortService.createCohort(this.newCohort).subscribe(() => {
      this.loadCohorts();
      this.closeModal();
    });
  }

  getStatusClass(status: string): string {
    switch(status) {
      case 'Active': return 'badge-green';
      case 'Completed': return 'badge-blue';
      case 'In Progress': return 'badge-orange';
      case 'Not Started': return 'badge-red';
      default: return 'badge-default';
    }
  }
}
