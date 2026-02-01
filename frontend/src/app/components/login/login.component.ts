import { Component, OnInit, NgZone, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/app/documents']);
    }
  }

  onSubmit() {
    this.error = '';
    this.loading = true;
    this.auth.login({ username: this.username, password: this.password }).subscribe({
      next: (res) => {
        this.ngZone.run(() => {
          this.loading = false;
          if (res) {
            this.router.navigate(['/app/documents']);
          } else {
            this.error = 'Credenciais inválidas. Tente novamente.';
            this.cdr.detectChanges();
          }
        });
      },
      error: () => {
        this.ngZone.run(() => {
          this.loading = false;
          this.error = 'Credenciais inválidas. Tente novamente.';
          this.cdr.detectChanges();
        });
      }
    });
  }
}
