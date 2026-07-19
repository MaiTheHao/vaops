import { inject, Injectable, signal } from '@angular/core';
import { AuthApiService } from '../../api/auth.api.service';
import { IdentityContextService } from '../../context/identity-context.service';
import { DialogFactoryService } from '../../shared/component/dialogs/dialog-factory.service';

@Injectable()
export class AuthService {
  readonly loading = signal(false);
  private readonly dialogService = inject(DialogFactoryService);

  constructor(
    private readonly authApi: AuthApiService,
    private readonly authContext: IdentityContextService
  ) {}

  login(accountName: string, password: string): void {
    if (!accountName) {
      this.dialogService.open('error', 'Lỗi đăng nhập', 'Tên tài khoản không được để trống.').subscribe();
      return;
    }
    if (password.length < 8) {
      this.dialogService.open('error', 'Lỗi đăng nhập', 'Mật khẩu phải có ít nhất 8 ký tự.').subscribe();
      return;
    }

    this.loading.set(true);
    this.authApi.login({ accountName, password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.authContext.setProfile({
          accountName,
          displayName: accountName
        });
        this.dialogService.open('info', 'Thông báo', 'Đăng nhập thành công!').subscribe();
      },
      error: (err) => {
        this.loading.set(false);
        const detail = err.error?.message || 'Login failed. Please check your credentials.';
        this.dialogService.open('error', 'Lỗi đăng nhập', detail).subscribe();
      }
    });
  }

  register(accountName: string, password: string, displayName: string, avatarUrl?: string): void {
    if (!accountName) {
      this.dialogService.open('error', 'Lỗi đăng ký', 'Tên tài khoản không được để trống.').subscribe();
      return;
    }
    if (password.length < 8) {
      this.dialogService.open('error', 'Lỗi đăng ký', 'Mật khẩu phải có ít nhất 8 ký tự.').subscribe();
      return;
    }
    if (!displayName) {
      this.dialogService.open('error', 'Lỗi đăng ký', 'Tên hiển thị không được để trống.').subscribe();
      return;
    }

    this.loading.set(true);
    this.authApi.register({
      accountName,
      password,
      displayName,
      avatarUrl: avatarUrl || undefined
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.dialogService.open('info', 'Thông báo', 'Đăng ký tài khoản thành công! Bạn đã có thể đăng nhập.').subscribe();
      },
      error: (err) => {
        this.loading.set(false);
        const detail = err.error?.message || 'Registration failed. Try a different account name.';
        this.dialogService.open('error', 'Lỗi đăng ký', detail).subscribe();
      }
    });
  }

  logout(): void {
    this.loading.set(true);
    this.authApi.logout().subscribe({
      next: () => {
        this.loading.set(false);
        this.authContext.clearProfile();
        this.dialogService.open('info', 'Thông báo', 'Đăng xuất thành công.').subscribe();
      },
      error: () => {
        this.loading.set(false);
        this.authContext.clearProfile();
        this.dialogService.open('info', 'Thông báo', 'Đăng xuất thành công.').subscribe();
      }
    });
  }
}
