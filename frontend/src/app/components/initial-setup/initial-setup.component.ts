import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SetupService } from '../../services/setup.service';

const NAME_MAX = 30;
const USERNAME_MAX = 15;
const PASSWORD_MIN = 6;
const PASSWORD_MAX = 10;

@Component({
  selector: 'app-initial-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './initial-setup.component.html',
  styleUrl: './initial-setup.component.css'
})
export class InitialSetupComponent implements OnInit {
  name = '';
  username = '';
  password = '';
  error = '';
  loading = false;
  checking = true;
  needsSetup = false;

  readonly nameMax = NAME_MAX;
  readonly usernameMax = USERNAME_MAX;
  readonly passwordMin = PASSWORD_MIN;
  readonly passwordMax = PASSWORD_MAX;

  constructor(
    private setup: SetupService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.setup.getStatus().subscribe({
      next: (res) => {
        this.checking = false;
        this.needsSetup = res.needsSetup;
        this.cdr.markForCheck();
        if (!this.needsSetup) {
          this.router.navigate(['/login']);
        }
      },
      error: () => {
        this.checking = false;
        this.error = 'Não foi possível verificar o status do sistema. Verifique se o backend está em execução (http://localhost:8080) e tente novamente.';
        this.cdr.markForCheck();
      }
    });
  }

  onSubmit() {
    this.error = '';
    const name = this.name.trim();
    const username = this.username.trim();
    if (!name || name.length > NAME_MAX) {
      this.error = `Nome é obrigatório e deve ter no máximo ${NAME_MAX} caracteres.`;
      return;
    }
    if (!username || username.length > USERNAME_MAX) {
      this.error = `Login é obrigatório e deve ter no máximo ${USERNAME_MAX} caracteres.`;
      return;
    }
    if (!this.password || this.password.length < PASSWORD_MIN || this.password.length > PASSWORD_MAX) {
      this.error = `Senha deve ter entre ${PASSWORD_MIN} e ${PASSWORD_MAX} caracteres.`;
      return;
    }
    this.loading = true;
    this.setup.createInitialAdmin({ name, username, password: this.password }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || err?.error?.error || 'Erro ao cadastrar. Tente novamente.';
      }
    });
  }
}
