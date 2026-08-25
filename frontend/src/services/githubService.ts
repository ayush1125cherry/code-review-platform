import api from './api';
import { GitHubRepo } from '../types/repo';

export const githubService = {
  async getAuthUrl(state?: string): Promise<{ url: string }> {
    const res = await api.get<{ url: string }>('/github/connect', { params: { state } });
    return res.data;
  },

  async connectToken(token: string): Promise<{ message: string; username: string; avatarUrl: string }> {
    const res = await api.post<{ message: string; username: string; avatarUrl: string }>('/github/connect-token', { token });
    return res.data;
  },

  async getRepositories(): Promise<GitHubRepo[]> {
    const res = await api.get<GitHubRepo[]>('/github/repositories');
    return res.data;
  },

  async disconnect(): Promise<{ message: string }> {
    const res = await api.delete<{ message: string }>('/github/disconnect');
    return res.data;
  }
};
