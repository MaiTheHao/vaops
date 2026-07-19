import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from './auth.service';
import { AuthContextService } from '../../context/auth-context.service';
import { DialogFactoryService } from '../../shared/component/dialogs/dialog-factory.service';
import {
  LucideCircleAlert,
  LucideCircleCheck,
  LucideLoader2,
  LucideUser,
  LucideLock,
  LucideEye,
  LucideEyeOff,
  LucideLink,
  LucideIdCard,
} from '@lucide/angular';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [
    FormsModule,
    LucideCircleAlert,
    LucideCircleCheck,
    LucideLoader2,
    LucideUser,
    LucideLock,
    LucideEye,
    LucideEyeOff,
    LucideLink,
    LucideIdCard,
    TranslatePipe,
  ],
  templateUrl: './auth.component.html',
  providers: [AuthService],
})
export class AuthComponent {
  private readonly dialogService = inject(DialogFactoryService);

  readonly mode = signal<'login' | 'register'>('login');
  readonly accountName = signal('');
  readonly password = signal('');
  readonly displayName = signal('');
  readonly avatarUrl = signal('');
  readonly showPassword = signal(false);
  readonly confirmPassword = signal('');
  readonly showConfirmPassword = signal(false);
  readonly isLoggedIn;
  readonly userProfile;
  readonly currentLang = signal('vi');

  constructor(
    readonly authService: AuthService,
    private readonly authContext: AuthContextService,
    private readonly translate: TranslateService,
  ) {
    this.isLoggedIn = this.authContext.isLoggedIn;
    this.userProfile = this.authContext.userProfile;
    this.translate.setFallbackLang('vi');
    const savedLang = localStorage.getItem('lang') || 'vi';
    this.translate.use(savedLang);
    this.currentLang.set(savedLang);
  }

  toggleMode() {
    this.mode.update((m) => (m === 'login' ? 'register' : 'login'));
    this.password.set('');
    this.confirmPassword.set('');
    this.showPassword.set(false);
    this.showConfirmPassword.set(false);
  }

  onSubmit() {
    const account = this.accountName().trim();
    const pwd = this.password();

    if (this.mode() === 'login') {
      this.authService.login(account, pwd);
    } else {
      if (pwd !== this.confirmPassword()) {
        this.dialogService.open('error', 'Lỗi đăng ký', 'Mật khẩu xác nhận không khớp.').subscribe();
        return;
      }
      this.authService.register(
        account,
        pwd,
        this.displayName().trim(),
        this.avatarUrl().trim() || undefined,
      );
    }
  }

  logout() {
    this.authService.logout();
    this.accountName.set('');
    this.password.set('');
    this.confirmPassword.set('');
    this.displayName.set('');
    this.avatarUrl.set('');
    this.showPassword.set(false);
    this.showConfirmPassword.set(false);
  }

  switchLanguage(lang: string) {
    this.translate.use(lang);
    this.currentLang.set(lang);
    localStorage.setItem('lang', lang);
  }
}
