import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { CohortsComponent } from './pages/cohorts/cohorts.component';
import { EvaluatorsComponent } from './pages/evaluators/evaluators.component';
import { MappingComponent } from './pages/mapping/mapping.component';
import { ReportsComponent } from './pages/reports/reports.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'cohorts', component: CohortsComponent, canActivate: [AuthGuard] },
  { path: 'evaluators', component: EvaluatorsComponent, canActivate: [AuthGuard] },
  { path: 'mapping/:cohortId', component: MappingComponent, canActivate: [AuthGuard] },
  { path: 'reports', component: ReportsComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: '/cohorts', pathMatch: 'full' },
  { path: '**', redirectTo: '/cohorts' }
];
