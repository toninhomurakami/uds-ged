import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, User, UserRequest, Role } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css'
})
export class UserListComponent implements OnInit {
  users = signal<User[]>([]);
  selected = signal<User | null>(null);
  loading = signal(false);
  error = signal('');
  editMode = signal(false);
  form = signal<UserRequest>({ name: '', username: '', password: '', role: 'USER' });
  showDeleteModal = signal(false);
  userToDelete = signal<User | null>(null);

  constructor(
    private userService: UserService,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.userService.list().subscribe({
      next: (list) => {
        this.users.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Erro ao carregar usuários.');
        this.loading.set(false);
      }
    });
  }

  select(user: User) {
    this.selected.set(user);
    this.editMode.set(true);
    this.form.set({
      name: user.name.toUpperCase(),
      username: user.username.toLowerCase(),
      password: '',
      role: user.role
    });
  }

  newUser() {
    this.selected.set(null);
    this.editMode.set(false);
    this.form.set({ name: '', username: '', password: '', role: 'USER' });
  }

  updateName(value: string) {
    this.form.update(f => ({ ...f, name: value.toUpperCase() }));
  }

  updateUsername(value: string) {
    this.form.update(f => ({ ...f, username: value.toLowerCase() }));
  }

  save() {
    const f = this.form();
    if (!f.name.trim() || !f.username.trim()) {
      this.error.set('Nome e usuário são obrigatórios.');
      return;
    }
    const isNew = !this.selected();
    if (isNew && (!f.password || f.password.length === 0)) {
      this.error.set('Senha é obrigatória para novo usuário.');
      return;
    }
    if (f.password && f.password.length > 10) {
      this.error.set('Senha deve ter no máximo 10 caracteres.');
      return;
    }
    this.error.set('');
    const id = this.selected()?.id;
    if (id) {
      this.userService.update(id, { name: f.name, username: f.username, password: f.password || undefined, role: f.role }).subscribe({
        next: () => { this.load(); this.newUser(); },
        error: (err) => this.error.set(err?.error?.message || 'Erro ao atualizar.')
      });
    } else {
      this.userService.create(f).subscribe({
        next: () => { this.load(); this.newUser(); },
        error: (err) => this.error.set(err?.error?.message || 'Erro ao cadastrar.')
      });
    }
  }

  openDeleteConfirm(user: User) {
    this.userToDelete.set(user);
    this.showDeleteModal.set(true);
  }

  closeDeleteConfirm() {
    this.showDeleteModal.set(false);
    this.userToDelete.set(null);
  }

  confirmDelete() {
    const user = this.userToDelete();
    if (!user) return;
    this.error.set('');
    this.userService.delete(user.id).subscribe({
      next: () => {
        this.closeDeleteConfirm();
        if (this.selected()?.id === user.id) {
          this.newUser();
        }
        this.load();
      },
      error: (err) => this.error.set(err?.error?.message || 'Erro ao excluir usuário.')
    });
  }
}
