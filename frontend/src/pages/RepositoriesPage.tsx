import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { githubService } from '../services/githubService';
import { repoService } from '../services/repoService';
import { GitHubRepo, RepositorySummary, RepoStatusResponse } from '../types/repo';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ProgressBar } from '../components/common/ProgressBar';
import { Navbar } from '../components/layout/Navbar';
import {
  Github,
  Search,
  Key,
  FolderPlus,
  ArrowRight,
  Sparkles,
  CheckCircle2,
  AlertCircle,
  Star,
  GitFork,
  Clock,
  Lock,
  Globe,
  Trash2
} from 'lucide-react';

export const RepositoriesPage: React.FC = () => {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();

  const [githubRepos, setGithubRepos] = useState<GitHubRepo[]>([]);
  const [localRepos, setLocalRepos] = useState<RepositorySummary[]>([]);
  const [loadingRepos, setLoadingRepos] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Manual import / token connection
  const [customRepoName, setCustomRepoName] = useState('');
  const [patToken, setPatToken] = useState('');
  const [connectingToken, setConnectingToken] = useState(false);
  const [tokenSuccess, setTokenSuccess] = useState<string | null>(null);
  const [tokenError, setTokenError] = useState<string | null>(null);

  // Active Job Progress tracking
  const [activeJob, setActiveJob] = useState<{
    repoId: number;
    repoName: string;
    status: string;
    step: string;
    progress: number;
    reviewId?: number;
    error?: string;
  } | null>(null);

  useEffect(() => {
    fetchRepos();
  }, [user?.githubConnected]);

  // Polling for active background job
  useEffect(() => {
    let intervalId: any = null;

    if (activeJob && activeJob.status !== 'READY' && activeJob.status !== 'FAILED') {
      intervalId = setInterval(async () => {
        try {
          const statusRes: RepoStatusResponse = await repoService.getRepositoryStatus(activeJob.repoId);
          setActiveJob(prev => prev ? ({
            ...prev,
            status: statusRes.status,
            step: statusRes.step || 'Processing...',
            progress: statusRes.progress || 10,
            reviewId: statusRes.reviewId,
            error: statusRes.errorMessage,
          }) : null);

          if (statusRes.status === 'READY' && statusRes.reviewId) {
            clearInterval(intervalId);
            setTimeout(() => {
              navigate(`/reviews/${statusRes.reviewId}`);
            }, 1200);
          }
        } catch (err) {
          console.error('Error polling repo status', err);
        }
      }, 1500);
    }

    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [activeJob?.repoId, activeJob?.status]);

  const fetchRepos = async () => {
    setLoadingRepos(true);
    try {
      const localList = await repoService.getRepositories();
      setLocalRepos(localList);

      const inProgress = localList.find(r => ['PENDING', 'IMPORTING', 'INDEXING', 'ANALYZING'].includes(r.status));
      if (inProgress) {
        setActiveJob({
          repoId: inProgress.id,
          repoName: inProgress.fullName,
          status: inProgress.status,
          step: inProgress.statusStep || 'Analyzing repository...',
          progress: inProgress.statusProgress || 20,
        });
      }

      if (user?.githubConnected) {
        const ghList = await githubService.getRepositories();
        setGithubRepos(ghList);
      }
    } catch (err) {
      console.error('Failed to fetch repositories', err);
    } finally {
      setLoadingRepos(false);
    }
  };

  const handleConnectToken = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!patToken.trim()) return;

    setConnectingToken(true);
    setTokenError(null);
    setTokenSuccess(null);

    try {
      const res = await githubService.connectToken(patToken.trim());
      setTokenSuccess(res.message);
      setPatToken('');
      await refreshUser();
      fetchRepos();
    } catch (err: any) {
      setTokenError(err.response?.data?.message || 'Invalid Personal Access Token');
    } finally {
      setConnectingToken(false);
    }
  };

  const handleStartReview = async (rawFullName: string, defaultBranch?: string) => {
    const cleanedName = rawFullName.trim()
      .replace(/^(https?:\/\/)?(www\.)?github\.com\//, '')
      .replace(/^git@github\.com:/, '')
      .replace(/\.git$/, '')
      .replace(/^\/+|\/+$/g, '');

    try {
      const summary = await repoService.importRepository({
        fullName: cleanedName,
        defaultBranch,
      });

      setActiveJob({
        repoId: summary.id,
        repoName: summary.fullName,
        status: summary.status,
        step: summary.statusStep || 'Queued for import & analysis...',
        progress: summary.statusProgress || 10,
      });
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to start repository analysis');
    }
  };

  const handleDeleteRepository = async (repoId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm('Are you sure you want to remove this repository and all its reviews? It will be returned to Browse GitHub Repositories.')) {
      return;
    }
    try {
      await repoService.deleteRepository(repoId);
      if (activeJob?.repoId === repoId) {
        setActiveJob(null);
      }
      await fetchRepos();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to remove repository');
    }
  };

  const filteredGhRepos = githubRepos.filter(r =>
    r.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    (r.language && r.language.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <div className="flex-1 flex flex-col min-w-0 bg-[#f8fafc]">
      <Navbar title="Repositories" />

      <div className="p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
        {/* Active Analysis Job Modal / Banner */}
        {activeJob && (
          <div className="bg-white border-2 border-indigo-500/40 rounded-2xl p-4 sm:p-6 shadow-xl space-y-4 animate-in fade-in">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="min-w-0">
                <h3 className="text-sm sm:text-base font-bold text-slate-900 truncate">
                  Analyzing: <span className="font-mono text-indigo-600">{activeJob.repoName}</span>
                </h3>
                <p className="text-[11px] sm:text-xs text-slate-500">
                  AI is generating comprehensive architectural & efficiency review...
                </p>
              </div>

              <div className="self-start sm:self-auto text-xs font-mono font-bold px-3 py-1 rounded-full bg-indigo-50 text-indigo-700 border border-indigo-200">
                {activeJob.status}
              </div>
            </div>

            <ProgressBar
              progress={activeJob.progress}
              stepText={activeJob.step}
              color={activeJob.status === 'FAILED' ? 'red' : activeJob.status === 'READY' ? 'green' : 'brand'}
            />

            {/* Stepper Display */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-2 text-xs">
              <div className={`p-2 sm:p-2.5 rounded-xl flex items-center space-x-2 ${activeJob.progress >= 30 ? 'text-emerald-700 bg-emerald-50 border border-emerald-200' : 'text-slate-400 bg-slate-50'}`}>
                <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                <span className="font-medium truncate">1. Imported</span>
              </div>
              <div className={`p-2 sm:p-2.5 rounded-xl flex items-center space-x-2 ${activeJob.progress >= 60 ? 'text-emerald-700 bg-emerald-50 border border-emerald-200' : 'text-slate-400 bg-slate-50'}`}>
                <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                <span className="font-medium truncate">2. Indexed</span>
              </div>
              <div className={`p-2 sm:p-2.5 rounded-xl flex items-center space-x-2 ${activeJob.progress >= 80 ? 'text-emerald-700 bg-emerald-50 border border-emerald-200' : 'text-slate-400 bg-slate-50'}`}>
                <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                <span className="font-medium truncate">3. AI Review</span>
              </div>
              <div className={`p-2 sm:p-2.5 rounded-xl flex items-center space-x-2 ${activeJob.status === 'READY' ? 'text-emerald-700 bg-emerald-50 border border-emerald-200' : 'text-slate-400 bg-slate-50'}`}>
                <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                <span className="font-medium truncate">4. Ready</span>
              </div>
            </div>

            {activeJob.status === 'FAILED' && activeJob.error && (
              <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center space-x-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{activeJob.error}</span>
              </div>
            )}
          </div>
        )}

        {/* GitHub Connection Banner & Direct Import Card */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* GitHub Connection Card */}
          <div className="lg:col-span-2 bg-white border border-slate-200/90 rounded-2xl p-5 sm:p-6 shadow-xs space-y-4">
            <div className="flex items-center space-x-3">
              <div className="p-2.5 rounded-xl bg-slate-100 text-slate-700 border border-slate-200 shrink-0">
                <Github className="w-5 h-5" />
              </div>
              <div className="min-w-0">
                <h3 className="font-bold text-slate-900 text-base truncate">Connect your GitHub account</h3>
                <p className="text-xs text-slate-500 leading-relaxed">
                  {user?.githubConnected
                    ? `Connected as @${user.githubUsername || user.username}. Access your private and public repositories.`
                    : 'Authorize via GitHub OAuth or provide a Personal Access Token to list your repositories.'}
                </p>
              </div>
            </div>

            {tokenError && (
              <p className="text-xs text-rose-600 bg-rose-50 p-2.5 rounded-xl border border-rose-200">{tokenError}</p>
            )}
            {tokenSuccess && (
              <p className="text-xs text-emerald-700 bg-emerald-50 p-2.5 rounded-xl border border-emerald-200">{tokenSuccess}</p>
            )}

            {!user?.githubConnected ? (
              <div className="space-y-3 pt-2">
                <form onSubmit={handleConnectToken} className="flex flex-col sm:flex-row gap-2">
                  <div className="relative flex-1">
                    <Key className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
                    <input
                      type="password"
                      value={patToken}
                      onChange={(e) => setPatToken(e.target.value)}
                      placeholder="Paste GitHub Personal Access Token (ghp_...)"
                      className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 placeholder-slate-400 text-xs focus:bg-white focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20"
                    />
                  </div>
                  <button
                    type="submit"
                    disabled={connectingToken || !patToken.trim()}
                    className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-900 text-white text-xs font-semibold transition-colors disabled:opacity-50 shrink-0"
                  >
                    {connectingToken ? 'Connecting...' : 'Connect Token'}
                  </button>
                </form>
              </div>
            ) : (
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pt-2">
                <div className="flex items-center space-x-2 text-xs text-emerald-600 font-medium">
                  <CheckCircle2 className="w-4 h-4 shrink-0" />
                  <span>GitHub account connected successfully</span>
                </div>
                <button
                  onClick={async () => {
                    await githubService.disconnect();
                    await refreshUser();
                    setGithubRepos([]);
                  }}
                  className="text-xs text-indigo-600 hover:text-indigo-800 font-semibold self-start sm:self-auto"
                >
                  Disconnect
                </button>
              </div>
            )}
          </div>

          {/* Quick Import Any Public Repository */}
          <div className="bg-white border border-slate-200/90 rounded-2xl p-5 sm:p-6 shadow-xs space-y-4">
            <div className="flex items-center space-x-3">
              <div className="p-2.5 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100 shrink-0">
                <FolderPlus className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-slate-900 text-base">Direct Repo Import</h3>
                <p className="text-xs text-slate-500">Review any public repository</p>
              </div>
            </div>

            <form
              onSubmit={(e) => {
                e.preventDefault();
                if (customRepoName.trim()) {
                  handleStartReview(customRepoName.trim());
                  setCustomRepoName('');
                }
              }}
              className="space-y-3 pt-1"
            >
              <input
                type="text"
                required
                value={customRepoName}
                onChange={(e) => setCustomRepoName(e.target.value)}
                placeholder="e.g. spring-projects/spring-petclinic"
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 placeholder-slate-400 text-xs focus:bg-white focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 font-mono"
              />
              <button
                type="submit"
                disabled={!customRepoName.trim()}
                className="w-full flex items-center justify-center space-x-2 py-2.5 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition-all disabled:opacity-50"
              >
                <span>Import & Review</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </form>
          </div>
        </div>

        {/* Analyzed / Imported Repositories */}
        {localRepos.length > 0 && (
          <div className="space-y-4">
            <div className="flex items-center space-x-2">
              <h2 className="text-lg font-bold text-slate-900">Analyzed Repositories</h2>
              <span className="text-xs px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-700 border border-slate-200 font-mono font-semibold">
                {localRepos.length}
              </span>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              {localRepos.map((repo) => (
                <div
                  key={repo.id}
                  className="bg-white hover:border-slate-300 border border-slate-200/90 rounded-2xl p-5 sm:p-6 transition-all shadow-xs flex flex-col justify-between space-y-4"
                >
                  <div className="space-y-2">
                    <div className="flex items-start justify-between gap-2">
                      <div className="min-w-0">
                        <h4 className="font-bold text-slate-900 text-sm truncate">{repo.name}</h4>
                        <p className="text-xs text-slate-500 font-mono truncate">{repo.fullName}</p>
                      </div>

                      <span className={`px-2.5 py-0.5 rounded-full text-xs font-mono font-bold border shrink-0 ${
                        repo.status === 'READY' ? 'bg-emerald-50 text-emerald-600 border-emerald-200' :
                        repo.status === 'FAILED' ? 'bg-rose-50 text-rose-600 border-rose-200' :
                        'bg-amber-50 text-amber-600 border-amber-200 animate-pulse'
                      }`}>
                        {repo.status}
                      </span>
                    </div>

                    {repo.description && (
                      <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                        {repo.description}
                      </p>
                    )}

                    <div className="flex flex-wrap items-center gap-2 text-xs text-slate-600 pt-1">
                      {repo.language && (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 border border-emerald-100 text-xs font-medium">
                          {repo.language}
                        </span>
                      )}
                      {repo.status === 'READY' && (
                        <span className="text-xs text-slate-500 italic">
                          Review ready! {repo.latestReviewScore ? `Overall Score: ${repo.latestReviewScore}/100` : ''}
                        </span>
                      )}
                      {repo.statusStep && repo.status !== 'READY' && (
                        <span className="text-[11px] text-slate-500 italic truncate max-w-[250px]">
                          {repo.statusStep}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="pt-4 border-t border-slate-100 flex flex-wrap items-center justify-between gap-2">
                    <button
                      onClick={(e) => handleDeleteRepository(repo.id, e)}
                      title="Remove Repository"
                      className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-slate-50 hover:bg-rose-50 text-slate-500 hover:text-rose-600 border border-slate-200 hover:border-rose-200 text-xs font-medium transition-colors"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span>Remove</span>
                    </button>

                    <div className="flex items-center space-x-2">
                      {repo.status === 'READY' && repo.latestReviewId && (
                        <button
                          onClick={() => navigate(`/reviews/${repo.latestReviewId}`)}
                          className="flex items-center space-x-1.5 px-3.5 sm:px-4 py-1.5 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white text-xs font-medium shadow-xs transition-all"
                        >
                          <span>Open Review</span>
                          <ArrowRight className="w-3.5 h-3.5" />
                        </button>
                      )}
                      <button
                        onClick={() => handleStartReview(repo.fullName, repo.defaultBranch)}
                        className="flex items-center space-x-1.5 px-3 sm:px-3.5 py-1.5 rounded-xl bg-[#6366f1] hover:bg-indigo-600 text-white text-xs font-medium shadow-xs transition-all"
                      >
                        <span>{repo.status === 'READY' ? 'Re-Analyze' : 'Retry'}</span>
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* GitHub Connected Repositories Section */}
        {user?.githubConnected && (
          <div className="space-y-4 pt-4 border-t border-slate-200">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <h2 className="text-lg font-bold text-slate-900">Browse GitHub Repositories</h2>

              <div className="relative w-full sm:w-72">
                <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search repositories..."
                  className="w-full pl-9 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-slate-900 placeholder-slate-400 text-xs focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20"
                />
              </div>
            </div>

            {loadingRepos ? (
              <div className="bg-white border border-slate-200 rounded-2xl p-8 sm:p-12 flex flex-col items-center justify-center space-y-3 shadow-xs">
                <LoadingSpinner size="lg" />
                <p className="text-xs text-slate-500">Fetching your GitHub repositories...</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                {filteredGhRepos.map((repo) => (
                  <div
                    key={repo.id}
                    className="bg-white hover:border-slate-300 border border-slate-200/90 rounded-2xl p-5 sm:p-6 transition-all shadow-xs flex flex-col justify-between space-y-4"
                  >
                    <div className="space-y-2">
                      <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0">
                          <div className="flex items-center space-x-2">
                            <h4 className="font-bold text-slate-900 text-sm truncate">{repo.name}</h4>
                            {repo.isPrivate ? (
                              <span title="Private repo"><Lock className="w-3 h-3 text-amber-500 shrink-0" /></span>
                            ) : (
                              <span title="Public repo"><Globe className="w-3 h-3 text-slate-400 shrink-0" /></span>
                            )}
                          </div>
                          <p className="text-xs text-slate-500 font-mono truncate">{repo.fullName}</p>
                        </div>

                        {repo.lastReviewScore && (
                          <span className="px-2 py-0.5 rounded-full text-[11px] font-mono font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 shrink-0">
                            Score: {repo.lastReviewScore}
                          </span>
                        )}
                      </div>

                      {repo.description && (
                        <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                          {repo.description}
                        </p>
                      )}

                      <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500 pt-1">
                        {repo.language && (
                          <span className="inline-flex items-center px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 border border-emerald-100 text-xs font-medium">
                            {repo.language}
                          </span>
                        )}
                        <span className="flex items-center space-x-1">
                          <Star className="w-3 h-3" />
                          <span>{repo.starsCount}</span>
                        </span>
                        <span className="flex items-center space-x-1">
                          <GitFork className="w-3 h-3" />
                          <span>{repo.forksCount}</span>
                        </span>
                        {repo.updatedAt && (
                          <span className="flex items-center space-x-1 text-[11px] text-slate-400">
                            <Clock className="w-3 h-3" />
                            <span>Updated {new Date(repo.updatedAt).toLocaleDateString()}</span>
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="pt-4 border-t border-slate-100 flex justify-end">
                      <button
                        onClick={() => handleStartReview(repo.fullName, repo.defaultBranch)}
                        className="flex items-center space-x-1.5 px-4 py-2 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition-all"
                      >
                        <span>Review Repository</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
