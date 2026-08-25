import api from './api';
import { AuthResponse, User } from '../types/auth';

export const authService = {
  async signup(data: { name: string; username: string; email: string; password: string }): Promise<AuthResponse> {
    const res = await api.post<AuthResponse>('/auth/signup', data);
    return res.data;
  },

  async login(data: { login: string; password: string }): Promise<AuthResponse> {
    const res = await api.post<AuthResponse>('/auth/login', data);
    return res.data;
  },

  async getCurrentUser(): Promise<User> {
    const res = await api.get<User>('/users/me');
    return res.data;
  },

  async updateProfile(data: { name: string; email: string; avatarUrl?: string; geminiApiKey?: string }): Promise<User> {
    const res = await api.put<User>('/users/profile', data);
    return res.data;
  },

  async changePassword(data: { currentPassword: string; newPassword: string }): Promise<{ message: string }> {
    const res = await api.put<{ message: string }>('/users/password', data);
    return res.data;
  },

  async getDashboardStats(): Promise<{ repositoriesReviewed: number; totalRepositories: number; questionsAsked: number; averageScore: number }> {
    const res = await api.get('/dashboard/stats');
    return res.data;
  }
};
