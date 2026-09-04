import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../services/report.service';
import { CohortService } from '../../services/cohort.service';
import { Cohort } from '../../models/cohort.model';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  reports: any[] = [];
  cohorts: Cohort[] = [];
  
  filters = {
    cohortId: 'All',
    stage: 'All',
    startDate: '',
    endDate: ''
  };

  constructor(
    private reportService: ReportService,
    private cohortService: CohortService
  ) {}

  ngOnInit(): void {
    this.loadCohorts();
    this.loadReports();
  }

  loadCohorts() {
    this.cohortService.getCohorts().subscribe(res => this.cohorts = res);
  }

  loadReports() {
    this.reportService.getMappingReport(this.filters).subscribe(res => {
      this.reports = res;
    });
  }

  onFilterChange() {
    this.loadReports();
  }

  exportCsv() {
    // Basic CSV export logic
    const header = ['Candidate', 'Evaluator', 'Cohort', 'Stage', 'Attempt', 'Mapped By', 'Mapped At'];
    const rows = this.reports.map(r => [
      r.candidate, r.evaluator, r.cohort, r.stage, r.attempt, r.mappedBy, r.mappedAt
    ]);
    const csvContent = "data:text/csv;charset=utf-8," 
        + [header.join(','), ...rows.map(e => e.join(','))].join("\n");
        
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "mapping_report.csv");
    document.body.appendChild(link); 
    link.click();
    document.body.removeChild(link);
  }
}
