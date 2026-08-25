export interface User {
  id: number;
  name: string;
  email: string;
  username: string;
  avatarUrl?: string;
  githubConnected: boolean;
  githubUsername?: string;
  hasCustomGeminiKey: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}
