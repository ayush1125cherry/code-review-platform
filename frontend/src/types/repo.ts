export type RepoStatus = 'PENDING' | 'IMPORTING' | 'INDEXING' | 'ANALYZING' | 'READY' | 'FAILED';

export interface ImportRepoRequest {
  fullName: string;
  defaultBranch?: string;
  customApiKey?: string;
}

export interface RepositorySummary {
  id: number;
  githubRepoId?: number;
  name: string;
  fullName: string;
  description?: string;
  defaultBranch: string;
  language?: string;
  htmlUrl?: string;
  isPrivate: boolean;
  starsCount: number;
  forksCount: number;
  sizeKb: number;
  status: RepoStatus;
  statusStep?: string;
  statusProgress: number;
  errorMessage?: string;
  latestReviewId?: number;
  latestReviewScore?: number;
  totalFiles: number;
  createdAt: string;
  updatedAt: string;
}

export interface GitHubRepo {
  id: number;
  name: string;
  fullName: string;
  description?: string;
  defaultBranch: string;
  language?: string;
  htmlUrl: string;
  isPrivate: boolean;
  starsCount: number;
  forksCount: number;
  sizeKb: number;
  updatedAt: string;
  alreadyImported: boolean;
  localRepoId?: number;
  lastReviewScore?: number;
}

export interface RepoStatusResponse {
  repositoryId: number;
  status: RepoStatus;
  step: string;
  progress: number;
  errorMessage?: string;
  reviewId?: number;
}

export interface RepositoryFile {
  id: number;
  repositoryId: number;
  filePath: string;
  fileName: string;
  fileExtension?: string;
  language?: string;
  lineCount: number;
  byteSize: number;
  content: string;
  isBinary: boolean;
}
