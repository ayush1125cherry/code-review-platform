import api from './api';
import { ImportRepoRequest, RepositoryFile, RepositorySummary, RepoStatusResponse } from '../types/repo';

export const repoService = {
  async importRepository(data: { fullName: string; defaultBranch?: string; customApiKey?: string }): Promise<RepositorySummary> {
    const res = await api.post<RepositorySummary>('/repositories/import', data);
    return res.data;
  },

  async getRepositories(): Promise<RepositorySummary[]> {
    const res = await api.get<RepositorySummary[]>('/repositories');
    return res.data;
  },

  async getRepository(id: number): Promise<RepositorySummary> {
    const res = await api.get<RepositorySummary>(`/repositories/${id}`);
    return res.data;
  },

  async getRepositoryStatus(id: number): Promise<RepoStatusResponse> {
    const res = await api.get<RepoStatusResponse>(`/repositories/${id}/status`);
    return res.data;
  },

  async getRepositoryFiles(id: number): Promise<RepositoryFile[]> {
    const res = await api.get<RepositoryFile[]>(`/repositories/${id}/files`);
    return res.data;
  },

  async getRepositoryFile(id: number, path: string): Promise<RepositoryFile> {
    const res = await api.get<RepositoryFile>(`/repositories/${id}/file`, { params: { path } });
    return res.data;
  },

  async deleteRepository(id: number): Promise<void> {
    await api.delete(`/repositories/${id}`);
  }
};
