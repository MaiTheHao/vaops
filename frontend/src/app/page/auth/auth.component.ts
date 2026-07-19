import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from './auth.service';
import { AuthContextService } from '../../context/auth-context.service';
import { DialogFactoryService } from '../../shared/component/dialogs/dialog-factory.service';

import { LanguageService } from '../../shared/service/language.service';
import { InputComponent } from '../../shared/component/input/input.component';
import { InputFactoryService } from '../../shared/component/input/input.factory';
import { PasswordInputComponent } from '../../shared/component/password-input/password-input.component';
import { SubmitButtonComponent } from '../../shared/component/submit-button/submit-button.component';
import { ButtonFactoryService } from '../../shared/component/submit-button/submit-button.factory';
import { TranslateKey } from '../../shared/const/translate-key.const';

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
export class AuthComponent {
  readonly authService = inject(AuthService);
  readonly langService = inject(LanguageService);
  readonly inputFactory = inject(InputFactoryService);
  readonly buttonFactory = inject(ButtonFactoryService);
  readonly dialogService = inject(DialogFactoryService);
  readonly authContext = inject(AuthContextService);

  readonly mode = signal<'login' | 'register'>('login');
  readonly accountName = signal('');
  readonly password = signal('');
  readonly displayName = signal('');
  readonly avatarUrl = signal('');
  readonly confirmPassword = signal('');
  readonly isLoggedIn = this.authContext.isLoggedIn;
  readonly userProfile = this.authContext.userProfile;

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
