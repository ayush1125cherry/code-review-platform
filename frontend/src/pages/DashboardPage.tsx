import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { reviewService } from '../services/reviewService';
import { authService } from '../services/authService';
import { ReviewResponse } from '../types/review';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { Navbar } from '../components/layout/Navbar';
import { CodeReviewLogoIcon } from '../components/common/Logo';
import {
  FileCheck2,
  MessageSquare,
  Award,
  Github,
  ArrowRight,
  GitBranch,
  Calendar,
  ChevronRight,
  Trash2
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [stats, setStats] = useState<{ repositoriesReviewed: number; totalRepositories: number; questionsAsked: number; averageScore: number }>({
    repositoriesReviewed: 0,
    totalRepositories: 0,
    questionsAsked: 0,
    averageScore: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [recentReviews, dashStats] = await Promise.all([
          reviewService.getRecentReviews(),
          authService.getDashboardStats(),
        ]);
        setReviews(recentReviews);
        setStats(dashStats);
      } catch (err) {
        console.error('Failed to load dashboard data', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const formatReviewDate = (dateVal: any): string => {
    if (!dateVal) return 'Recently';
    if (Array.isArray(dateVal)) {
      const [year, month, day] = dateVal;
      return new Date(year, (month || 1) - 1, day || 1).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    }
    const parsed = new Date(dateVal);
    if (isNaN(parsed.getTime())) return 'Recently';
    return parsed.toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  };

  const handleDeleteReview = async (reviewId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm('Are you sure you want to remove this review?')) return;
    try {
      await reviewService.deleteReview(reviewId);
      setReviews(prev => prev.filter(r => r.id !== reviewId));
      setStats(prev => ({
        ...prev,
        repositoriesReviewed: Math.max(0, prev.repositoriesReviewed - 1),
      }));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to remove review');
    }
  };

  const getScoreBadge = (score: number) => {
    if (score >= 80) return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    if (score >= 65) return 'bg-indigo-50 text-indigo-700 border-indigo-200';
    if (score >= 50) return 'bg-amber-50 text-amber-700 border-amber-200';
    return 'bg-rose-50 text-rose-700 border-rose-200';
  };

  return (
    <div className="flex-1 flex flex-col min-w-0 bg-[#f8fafc]">
      <Navbar title="Dashboard" />

      <div className="p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
        {/* Welcome Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 sm:p-8 rounded-2xl border border-slate-200/90 shadow-xs relative overflow-hidden">
          <div className="flex items-start space-x-4 z-10">
            <CodeReviewLogoIcon className="w-12 h-12 sm:w-14 sm:h-14 shrink-0 hidden sm:block" />
            <div className="space-y-1">
              <div className="text-xs font-semibold text-indigo-600 uppercase tracking-wider">
                <span>AI Code Review Platform</span>
              </div>
              <h1 className="text-xl sm:text-2xl lg:text-3xl font-extrabold text-slate-900 tracking-tight">
                Welcome back, {user?.name}!
              </h1>
              <p className="text-xs sm:text-sm text-slate-500 max-w-xl leading-relaxed">
                Import any GitHub repository to analyze architecture, audit code efficiency, identify vulnerabilities, and chat interactively with AI.
              </p>
            </div>
          </div>

          <div className="flex flex-wrap gap-2.5 sm:gap-3 z-10">
            {!user?.githubConnected && (
              <button
                onClick={() => navigate('/repositories')}
                className="flex items-center space-x-2 px-3.5 sm:px-4 py-2 sm:py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold border border-slate-200 transition-colors"
              >
                <Github className="w-4 h-4" />
                <span>Connect GitHub</span>
              </button>
            )}

            <button
              onClick={() => navigate('/repositories')}
              className="flex items-center space-x-2 px-4 sm:px-5 py-2 sm:py-2.5 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition-all"
            >
              <span>Review Repository</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3.5 sm:gap-5">
          <div className="bg-white border border-slate-200/90 rounded-2xl p-4 sm:p-5 shadow-xs flex items-center space-x-4">
            <div className="p-3 sm:p-3.5 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100 shrink-0">
              <FileCheck2 className="w-5 h-5 sm:w-6 sm:h-6" />
            </div>
            <div>
              <p className="text-[11px] sm:text-xs font-semibold text-slate-400 uppercase tracking-wider">Repositories Reviewed</p>
              <h3 className="text-xl sm:text-2xl font-extrabold text-slate-900 mt-0.5">{stats.repositoriesReviewed}</h3>
            </div>
          </div>

          <div className="bg-white border border-slate-200/90 rounded-2xl p-4 sm:p-5 shadow-xs flex items-center space-x-4">
            <div className="p-3 sm:p-3.5 rounded-xl bg-purple-50 text-purple-600 border border-purple-100 shrink-0">
              <MessageSquare className="w-5 h-5 sm:w-6 sm:h-6" />
            </div>
            <div>
              <p className="text-[11px] sm:text-xs font-semibold text-slate-400 uppercase tracking-wider">AI Questions Asked</p>
              <h3 className="text-xl sm:text-2xl font-extrabold text-slate-900 mt-0.5">{stats.questionsAsked}</h3>
            </div>
          </div>

          <div className="bg-white border border-slate-200/90 rounded-2xl p-4 sm:p-5 shadow-xs flex items-center space-x-4">
            <div className="p-3 sm:p-3.5 rounded-xl bg-emerald-50 text-emerald-600 border border-emerald-100 shrink-0">
              <Award className="w-5 h-5 sm:w-6 sm:h-6" />
            </div>
            <div>
              <p className="text-[11px] sm:text-xs font-semibold text-slate-400 uppercase tracking-wider">Average Repo Score</p>
              <h3 className="text-xl sm:text-2xl font-extrabold text-slate-900 mt-0.5">
                {stats.averageScore > 0 ? `${stats.averageScore} / 100` : 'N/A'}
              </h3>
            </div>
          </div>
        </div>

        {/* Recent Reviews Section */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base sm:text-lg font-bold text-slate-900 flex items-center space-x-2">
              <GitBranch className="w-4 h-4 sm:w-5 sm:h-5 text-indigo-600" />
              <span>Your Repositories & Reviews</span>
            </h2>
            <button
              onClick={() => navigate('/repositories')}
              className="text-xs font-semibold text-indigo-600 hover:text-indigo-700 flex items-center space-x-1"
            >
              <span>View All</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          {loading ? (
            <div className="bg-white border border-slate-200 rounded-2xl p-8 sm:p-12 flex flex-col items-center justify-center space-y-3 shadow-xs">
              <LoadingSpinner size="lg" />
              <p className="text-xs text-slate-500">Loading your repository reviews...</p>
            </div>
          ) : reviews.length === 0 ? (
            <div className="bg-white border border-slate-200/90 rounded-2xl p-8 sm:p-12 text-center space-y-4 shadow-xs">
              <div className="w-12 h-12 rounded-2xl bg-slate-100 border border-slate-200 flex items-center justify-center mx-auto text-slate-400">
                <FileCheck2 className="w-6 h-6" />
              </div>
              <div className="space-y-1">
                <h3 className="text-sm font-bold text-slate-800">No repositories reviewed yet</h3>
                <p className="text-xs text-slate-500 max-w-sm mx-auto">
                  Connect your GitHub account or enter any public repository to generate your first AI code review.
                </p>
              </div>
              <button
                onClick={() => navigate('/repositories')}
                className="px-4 py-2 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition-all"
              >
                Import Repository
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              {reviews.map((rev) => (
                <div
                  key={rev.id}
                  className="bg-white hover:border-slate-300 border border-slate-200/90 rounded-2xl p-5 sm:p-6 transition-all shadow-xs flex flex-col justify-between space-y-4"
                >
                  <div className="space-y-2">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <h4 className="font-bold text-slate-900 text-sm sm:text-base truncate">{rev.repositoryName}</h4>
                        <p className="text-xs text-slate-500 font-mono truncate">{rev.repositoryFullName}</p>
                      </div>
                      <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold font-mono border shrink-0 ${getScoreBadge(rev.overallScore)}`}>
                        Score: {rev.overallScore}
                      </span>
                    </div>

                    <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                      {rev.summary}
                    </p>

                    <div className="flex flex-wrap gap-1.5 pt-1">
                      {rev.language && (
                        <span className="px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 text-[11px] font-medium border border-emerald-100">
                          {rev.language}
                        </span>
                      )}
                      {rev.technologies?.slice(0, 3).map((t, idx) => (
                        <span key={idx} className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 text-[11px] border border-slate-200">
                          {t}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div className="pt-4 border-t border-slate-100 flex items-center justify-between">
                    <span className="text-[11px] text-slate-400 flex items-center space-x-1">
                      <Calendar className="w-3 h-3 text-slate-400" />
                      <span>{formatReviewDate(rev.createdAt)}</span>
                    </span>

                    <div className="flex items-center space-x-2">
                      <button
                        onClick={(e) => handleDeleteReview(rev.id, e)}
                        title="Remove Review"
                        className="p-1.5 rounded-lg bg-slate-50 hover:bg-rose-50 text-slate-400 hover:text-rose-600 border border-slate-200 hover:border-rose-200 transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>

                      <button
                        onClick={() => navigate(`/reviews/${rev.id}`)}
                        className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition-colors"
                      >
                        <span>Open Review</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
