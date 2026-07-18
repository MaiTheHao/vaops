export interface LoginRequestDto {
  accountName: string;
  password: string;
}

export interface RegisterRequestDto {
  accountName: string;
  password: string;
  displayName: string;
  avatarUrl?: string;
}

export interface RegisterResponseDto {
  id: string;
  accountName: string;
  displayName: string;
  avatarUrl?: string;
}

export interface UserProfile {
  accountName: string;
  displayName: string;
  avatarUrl?: string;
}
