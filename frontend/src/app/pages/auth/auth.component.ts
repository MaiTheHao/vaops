import { Component, computed, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from './auth.service';
import { IdentityContextService } from '../../core/context/identity-context.service';
import { DialogFactoryService } from '../../shared/components/dialogs/dialog-factory.service';
import { EventManager } from '../../shared/services/event-manager.service';
import { AppEventKey } from '../../shared/constants/app-event.const';
import { UserProfile } from '../../core/models/profile.model';
import { Subscription } from 'rxjs';

import { LanguageService } from '../../shared/services/language.service';
import { InputComponent } from '../../shared/components/input/input.component';
import { InputFactoryService } from '../../shared/components/input/input.factory';
import { PasswordInputComponent } from '../../shared/components/password-input/password-input.component';
import { SubmitButtonComponent } from '../../shared/components/submit-button/submit-button.component';
import { ButtonFactoryService } from '../../shared/components/submit-button/submit-button.factory';
import { TranslateKey } from '../../shared/constants/translate-key.const';

import { LucideUser, LucideIdCard, LucideLink, LucideLock } from '@lucide/angular';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [
    FormsModule,
    TranslatePipe,
    InputComponent,
    PasswordInputComponent,
    SubmitButtonComponent,
  ],
  templateUrl: './auth.component.html',
  providers: [AuthService],
})
export class AuthComponent implements OnInit, OnDestroy {
  readonly authService = inject(AuthService);
  readonly langService = inject(LanguageService);
  readonly inputFactory = inject(InputFactoryService);
  readonly buttonFactory = inject(ButtonFactoryService);
  readonly dialogService = inject(DialogFactoryService);
  readonly authContext = inject(IdentityContextService);
  private readonly eventManager = inject(EventManager);

  readonly mode = signal<'login' | 'register'>('login');
  readonly accountName = signal('');
  readonly password = signal('');
  readonly displayName = signal('');
  readonly avatarUrl = signal('');
  readonly confirmPassword = signal('');
  readonly userProfile = this.authContext.userProfile;
  readonly lastSyncedTime = signal<string | null>(null);

  private sub?: Subscription;

  ngOnInit() {
    this.sub = this.eventManager.listen<UserProfile>(AppEventKey.PROFILE_SYNCED).subscribe(() => {
      const now = new Date().toLocaleTimeString();
      this.lastSyncedTime.set(now);
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  readonly accountNameCfg = computed(() => {
    this.langService.currentLang();
    return this.inputFactory.createTextConfig(
      { component: LucideUser, position: 'left', cssClass: 'size-5' },
      {
        label: this.langService.translate(TranslateKey.auth.label.accountName),
        placeholder: this.langService.translate(TranslateKey.auth.placeholder.accountName),
        required: true,
      },
    );
  });
  readonly displayNameCfg = computed(() => {
    this.langService.currentLang();
    return this.inputFactory.createTextConfig(
      { component: LucideIdCard, position: 'left', cssClass: 'size-5' },
      {
        label: this.langService.translate(TranslateKey.auth.label.displayName),
        placeholder: this.langService.translate(TranslateKey.auth.placeholder.displayName),
        required: true,
      },
    );
  });
  readonly passwordCfg = computed(() => {
    this.langService.currentLang();
    return this.inputFactory.createPasswordConfig(
      { component: LucideLock, position: 'left', cssClass: 'size-5' },
      { label: this.langService.translate(TranslateKey.auth.label.password), required: true },
    );
  });
  readonly confirmPasswordCfg = computed(() => {
    this.langService.currentLang();
    return this.inputFactory.createPasswordConfig(
      { component: LucideLock, position: 'left', cssClass: 'size-5' },
      { label: this.langService.translate(TranslateKey.auth.label.confirmPassword), required: true },
    );
  });
  readonly avatarUrlCfg = computed(() => {
    this.langService.currentLang();
    return this.inputFactory.createUrlConfig(
      { component: LucideLink, position: 'left', cssClass: 'size-5' },
      {
        label: this.langService.translate(TranslateKey.auth.label.avatarUrl),
        placeholder: 'https://example.com/avatar.png',
        css: { container: 'flex flex-col gap-2 col-span-full' },
      },
    );
  });

  readonly submitLabel = computed(() =>
    this.mode() === 'login'
      ? this.langService.translate(TranslateKey.auth.btn.loginSubmit)
      : this.langService.translate(TranslateKey.auth.btn.registerSubmit),
  );

  toggleMode() {
    this.mode.update(m => (m === 'login' ? 'register' : 'login'));
    this.password.set('');
    this.confirmPassword.set('');
  }

  onSubmit() {
    const account = this.accountName().trim();
    const pwd = this.password();

    if (this.mode() === 'login') {
      this.authService.login(account, pwd);
    } else {
      if (pwd !== this.confirmPassword()) {
        this.dialogService.open(
          'error',
          this.langService.translate(TranslateKey.auth.dialog.registerError),
          this.langService.translate(TranslateKey.auth.dialog.passwordMismatch),
        ).subscribe();
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
  }
}
