import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { LayoutComponent } from './components/layout/layout.component';
import { LoginComponent } from './components/login/login.component';
import { InitialSetupComponent } from './components/initial-setup/initial-setup.component';
import { DocumentListComponent } from './components/document-list/document-list.component';
import { DocumentDetailComponent } from './components/document-detail/document-detail.component';
import { DocumentCreateComponent } from './components/document-create/document-create.component';
import { UserListComponent } from './components/user-list/user-list.component';

export const routes: Routes = [
  { path: '', component: InitialSetupComponent },
  { path: 'login', component: LoginComponent },
  {
    path: 'app',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'documents', pathMatch: 'full' },
      { path: 'documents', component: DocumentListComponent },
      { path: 'documents/new', component: DocumentCreateComponent },
      { path: 'documents/:id', component: DocumentDetailComponent },
      { path: 'users', component: UserListComponent, canActivate: [adminGuard] }
    ]
  },
  { path: '**', redirectTo: '' }
];
