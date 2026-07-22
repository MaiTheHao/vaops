export interface UserProfile {
  id: string;
  accountName: string | null;
  displayName: string | null;
  avatarUrl: string | null;
  lastLoginAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateProfileReq {
  displayName: string;
  avatarUrl: string;
}

export interface ChangePasswordReq {
  oldPassword: string;
  newPassword: string;
}
