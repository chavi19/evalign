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

  loading: boolean = false;
  loadingMore: boolean = false;
  hasMore: boolean = false;
  page: number = 0;
  pageSize: number = 15;

  constructor(
    private reportService: ReportService,
    private cohortService: CohortService
  ) {}

  ngOnInit(): void {
    this.loadCohorts();
    this.loadReports(true);
  }

  loadCohorts(): void {
    this.cohortService.getCohorts().subscribe({
      next: (res) => this.cohorts = res || [],
      error: (err) => console.error('Failed to load cohorts', err)
    });
  }

  loadReports(reset: boolean = true): void {
    if (reset) {
      this.page = 0;
      this.loading = true;
      this.reports = [];
      this.filteredReports = [];
    } else {
      this.loadingMore = true;
    }

    this.reportService.getReports({
      page: this.page,
      size: this.pageSize,
      search: this.filters.search ? this.filters.search.trim() : undefined,
      cohortName: this.filters.cohortName !== 'All' ? this.filters.cohortName : undefined,
      stage: this.filters.stage !== 'All' ? this.filters.stage : undefined,
      status: this.filters.status !== 'All' ? this.filters.status : undefined
    }).subscribe({
      next: (res) => {
        const batch = res || [];
        if (reset) {
          this.reports = batch;
        } else {
          this.reports = [...this.reports, ...batch];
        }
        this.filteredReports = this.reports;
        this.hasMore = batch.length === this.pageSize;
        this.loading = false;
        this.loadingMore = false;
      },
      error: (err) => {
        console.error('Failed to load reports', err);
        this.loading = false;
        this.loadingMore = false;
      }
    });
  }

  viewMore(): void {
    if (!this.hasMore || this.loading || this.loadingMore) return;
    this.page++;
    this.loadReports(false);
  }

  onFilterChange(): void {
    this.loadReports(true);
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
