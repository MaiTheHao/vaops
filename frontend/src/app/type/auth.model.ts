/** Maps to backend LoginWebRequestDto */
export interface LoginRequestDto {
  accountName: string;
  password: string;
}

/** Maps to backend RegisterWebRequestDto */
export interface RegisterRequestDto {
  accountName: string;
  password: string;
  displayName: string;
  avatarUrl?: string;
}

/** Maps to backend RegisterWebResponseDto */
export interface RegisterResponseDto {
  id: string;
  accountName: string;
  displayName: string;
  avatarUrl?: string;
}

/** Local user profile model (for future use with profile endpoint) */
export interface UserProfile {
  accountName: string;
  displayName: string;
  avatarUrl?: string;
}
