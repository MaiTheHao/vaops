import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthContextService } from '../../context/auth-context.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css'
})
export class AuthComponent {
  readonly mode = signal<'login' | 'register'>('login');
  readonly accountName = signal('');
  readonly password = signal('');
  readonly displayName = signal('');
  readonly avatarUrl = signal('');

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly isLoggedIn;
  readonly userProfile;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly authContext: AuthContextService
  ) {
    this.isLoggedIn = this.authContext.isLoggedIn;
    this.userProfile = this.authContext.userProfile;
  }

  toggleMode() {
    this.mode.update(m => m === 'login' ? 'register' : 'login');
    this.errorMessage.set('');
    this.successMessage.set('');
    this.password.set('');
  }

  onSubmit() {
    this.errorMessage.set('');
    this.successMessage.set('');

    const account = this.accountName().trim();
    const pwd = this.password();

    if (!account) {
      this.errorMessage.set('Account name is required.');
      return;
    }

    if (pwd.length < 8) {
      this.errorMessage.set('Password must be at least 8 characters long.');
      return;
    }

    this.loading.set(true);

    if (this.mode() === 'login') {
      this.authService.login({ accountName: account, password: pwd }).subscribe({
        next: () => {
          this.loading.set(false);
          this.authContext.setLogin({
            accountName: account,
            displayName: account
          });
          this.successMessage.set('Logged in successfully!');
        },
        error: (err) => {
          this.loading.set(false);
          const detail = err.error?.message || 'Login failed. Please check your credentials.';
          this.errorMessage.set(detail);
        }
      });
    } else {
      const name = this.displayName().trim();
      const avatar = this.avatarUrl().trim();

      if (!name) {
        this.errorMessage.set('Display name is required.');
        this.loading.set(false);
        return;
      }

      this.authService.register({
        accountName: account,
        password: pwd,
        displayName: name,
        avatarUrl: avatar || undefined
      }).subscribe({
        next: (res) => {
          this.loading.set(false);
          this.successMessage.set('Registration successful! You can now log in.');
          this.mode.set('login');
          this.password.set('');
        },
        error: (err) => {
          this.loading.set(false);
          const detail = err.error?.message || 'Registration failed. Try a different account name.';
          this.errorMessage.set(detail);
        }
      });
    }
  }

  logout() {
    this.authContext.setLogout();
    this.accountName.set('');
    this.password.set('');
    this.displayName.set('');
    this.avatarUrl.set('');
    this.errorMessage.set('');
    this.successMessage.set('Logged out successfully.');
  }
}