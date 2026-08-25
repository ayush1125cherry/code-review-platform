export type FindingSeverity = 'INFO' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type FindingCategory = 'ARCHITECTURE' | 'CODE_QUALITY' | 'EFFICIENCY' | 'SECURITY' | 'MAINTAINABILITY' | 'TESTING' | 'DOCUMENTATION' | 'STRENGTH' | 'WEAKNESS';

export interface CodeReference {
  file: string;
  path: string;
  startLine?: number;
  endLine?: number;
  snippet?: string;
  comment?: string;
}

export interface ReviewFinding {
  id?: number;
  category: FindingCategory;
  severity: FindingSeverity;
  title: string;
  description: string;
  recommendation?: string;
  references: CodeReference[];
}

export interface ScoreBreakdown {
  overall: number;
  architecture: number;
  codeQuality: number;
  efficiency: number;
  security: number;
  maintainability: number;
  testing: number;
  documentation: number;
}

export interface CategoryReview {
  name: string;
  score: number;
  summary: string;
  findings: ReviewFinding[];
  references: CodeReference[];
}

export interface ReviewResponse {
  id: number;
  repositoryId: number;
  repositoryName: string;
  repositoryFullName: string;
  defaultBranch: string;
  language?: string;
  conversationId?: number;

  overallScore: number;
  scores: ScoreBreakdown;

  repositoryType: string;
  summary: string;
  technologies: string[];
  architecture: string;

  efficiency: CategoryReview;
  security: CategoryReview;
  maintainability: CategoryReview;
  testing: CategoryReview;
  documentation: CategoryReview;

  strengths: ReviewFinding[];
  weaknesses: ReviewFinding[];
  allFindings: ReviewFinding[];
  allReferences: CodeReference[];

  createdAt: string;
}
