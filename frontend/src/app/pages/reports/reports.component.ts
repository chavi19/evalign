import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../services/report.service';
import { CohortService } from '../../services/cohort.service';
import { Cohort } from '../../models/cohort.model';
import { Mapping } from '../../models/mapping.model';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  reports: Mapping[] = [];
  filteredReports: Mapping[] = [];
  cohorts: Cohort[] = [];
  
  filters = {
    cohortName: 'All',
    stage: 'All',
    status: 'All',
    search: ''
  };

  constructor(
    private reportService: ReportService,
    private cohortService: CohortService
  ) {}

  ngOnInit(): void {
    this.loadCohorts();
    this.loadReports();
  }

  loadCohorts(): void {
    this.cohortService.getCohorts().subscribe({
      next: (res) => this.cohorts = res || [],
      error: (err) => console.error('Failed to load cohorts', err)
    });
  }

  loadReports(): void {
    this.reportService.getReports().subscribe({
      next: (res) => {
        this.reports = res || [];
        this.applyFilters();
      },
      error: (err) => console.error('Failed to load reports', err)
    });
  }

  applyFilters(): void {
    let list = [...this.reports];

    if (this.filters.search) {
      const q = this.filters.search.toLowerCase();
      list = list.filter(r => 
        (r.candidateName && r.candidateName.toLowerCase().includes(q)) ||
        (r.evaluatorName && r.evaluatorName.toLowerCase().includes(q))
      );
    }

    if (this.filters.cohortName && this.filters.cohortName !== 'All') {
      list = list.filter(r => r.cohortName === this.filters.cohortName);
    }

    if (this.filters.stage && this.filters.stage !== 'All') {
      list = list.filter(r => r.round && r.round.toUpperCase() === this.filters.stage.toUpperCase());
    }

    if (this.filters.status && this.filters.status !== 'All') {
      list = list.filter(r => r.status && r.status.toUpperCase() === this.filters.status.toUpperCase());
    }

    this.filteredReports = list;
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  exportCsv(): void {
    const header = ['Candidate', 'Evaluator', 'Cohort', 'Stage', 'Attempt', 'Status', 'Mapped By', 'Mapped At'];
    const rows = this.filteredReports.map(r => [
      `"${r.candidateName || ''}"`,
      `"${r.evaluatorName || ''}"`,
      `"${r.cohortName || ''}"`,
      `"${r.round || ''}"`,
      `"${r.attempt || 1}"`,
      `"${r.status || ''}"`,
      `"${r.mappedByName || ''}"`,
      `"${r.mappedAt || ''}"`
    ]);
    const csvContent = 'data:text/csv;charset=utf-8,' + [header.join(','), ...rows.map(e => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', 'evaluator_mapping_report.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
