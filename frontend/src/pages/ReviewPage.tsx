import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { reviewService } from '../services/reviewService';
import { ReviewResponse, CodeReference } from '../types/review';
import { ScoreCard } from '../components/review/ScoreCard';
import { ReviewOverview } from '../components/review/ReviewOverview';
import { EfficiencySection } from '../components/review/EfficiencySection';
import { StrengthsWeaknesses } from '../components/review/StrengthsWeaknesses';
import { CategoryTabs } from '../components/review/CategoryTabs';
import { ChatInterface } from '../components/chat/ChatInterface';
import { CodeViewerModal } from '../components/code/CodeViewerModal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { Navbar } from '../components/layout/Navbar';
import {
  ArrowLeft,
  FolderGit2,
  Calendar,
  Share2,
  Check
} from 'lucide-react';

export const ReviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [review, setReview] = useState<ReviewResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Selected file for Code Viewer modal
  const [selectedFileRef, setSelectedFileRef] = useState<CodeReference | null>(null);
  const [copiedLink, setCopiedLink] = useState<boolean>(false);

  useEffect(() => {
    let isMounted = true;

    const fetchReview = async () => {
      if (!id) return;
      setLoading(true);
      setError(null);
      try {
        const data = await reviewService.getReview(parseInt(id, 10));
        if (isMounted) {
          setReview(data);
        }
      } catch (err: any) {
        if (isMounted) {
          setError(err.response?.data?.message || 'Failed to load code review');
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchReview();
    return () => {
      isMounted = false;
    };
  }, [id]);

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

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 2000);
  };

  const handleViewFile = (ref: CodeReference) => {
    setSelectedFileRef(ref);
  };

  if (loading) {
    return (
      <div className="flex-1 flex flex-col min-w-0 bg-[#f8fafc]">
        <Navbar />
        <div className="flex-1 flex flex-col items-center justify-center space-y-4">
          <LoadingSpinner size="lg" />
          <p className="text-sm text-slate-500">Loading comprehensive code review...</p>
        </div>
      </div>
    );
  }

  if (error || !review) {
    return (
      <div className="flex-1 flex flex-col min-w-0 bg-[#f8fafc]">
        <Navbar />
        <div className="p-6 sm:p-8 max-w-xl mx-auto my-auto text-center space-y-4">
          <div className="p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700">
            <h3 className="font-bold text-base">Error Loading Review</h3>
            <p className="text-xs mt-1">{error || 'Review not found'}</p>
          </div>
          <button
            onClick={() => navigate('/repositories')}
            className="px-4 py-2 rounded-xl bg-slate-800 text-white text-xs font-semibold hover:bg-slate-900"
          >
            Back to Repositories
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col min-w-0 bg-[#f8fafc]">
      <Navbar title={review ? `Review: ${review.repositoryName}` : 'Review'} />

      {/* Top Banner Header */}
      <div className="px-4 sm:px-6 lg:px-8 py-3.5 sm:py-4 border-b border-slate-200/90 bg-white flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 sm:gap-4">
        <div className="flex items-center space-x-3 min-w-0 w-full sm:w-auto">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-600 border border-slate-200 transition-colors shrink-0"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>

          <div className="min-w-0 flex-1">
            <div className="flex items-center space-x-2">
              <FolderGit2 className="w-4 h-4 text-indigo-600 shrink-0" />
              <h2 className="font-extrabold text-slate-900 text-sm sm:text-base lg:text-lg truncate">
                {review.repositoryFullName}
              </h2>
              <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-600 text-[10px] sm:text-[11px] font-mono border border-slate-200 shrink-0">
                {review.defaultBranch}
              </span>
            </div>
            <div className="flex items-center space-x-2 sm:space-x-3 text-[11px] sm:text-xs text-slate-500 mt-0.5">
              <span className="flex items-center space-x-1">
                <Calendar className="w-3 h-3" />
                <span>Reviewed {formatReviewDate(review.createdAt)}</span>
              </span>
              {review.language && (
                <span className="truncate">• {review.language}</span>
              )}
            </div>
          </div>
        </div>

        <div className="flex items-center space-x-3 shrink-0 self-end sm:self-auto">
          <button
            onClick={handleShare}
            className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold border border-slate-200 transition-colors"
          >
            {copiedLink ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Share2 className="w-3.5 h-3.5" />}
            <span>{copiedLink ? 'Link Copied' : 'Share Review'}</span>
          </button>
        </div>
      </div>

      {/* Main Dual-Pane / Split View */}
      <div className="p-4 sm:p-6 lg:p-8 max-w-[1700px] w-full mx-auto flex-1">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-start">
          {/* Left Column: Comprehensive Review Breakdown (7 cols) */}
          <div className="lg:col-span-7 space-y-6">
            {/* Score Card with 7 categories */}
            <ScoreCard scores={review.scores} />

            {/* Repository Overview */}
            <ReviewOverview review={review} />

            {/* Code Efficiency Section */}
            <EfficiencySection efficiency={review.efficiency} onViewFile={handleViewFile} />

            {/* Strengths & Weaknesses */}
            <StrengthsWeaknesses
              strengths={review.strengths}
              weaknesses={review.weaknesses}
              onViewFile={handleViewFile}
            />

            {/* Category Tabs (Security, Maintainability, Testing, Docs) */}
            <CategoryTabs
              security={review.security}
              maintainability={review.maintainability}
              testing={review.testing}
              documentation={review.documentation}
              onViewFile={handleViewFile}
            />
          </div>

          {/* Right Column: Interactive ChatGPT-Style Chat Pane (5 cols, sticky on lg) */}
          <div className="lg:col-span-5 lg:sticky lg:top-4">
            <ChatInterface
              reviewId={review.id}
              initialConversationId={review.conversationId}
              efficiencyScore={review.scores.efficiency}
              onViewFile={handleViewFile}
            />
          </div>
        </div>
      </div>

      {/* Code Viewer Modal */}
      {selectedFileRef && (
        <CodeViewerModal
          repositoryId={review.repositoryId}
          filePath={selectedFileRef.path}
          startLine={selectedFileRef.startLine}
          endLine={selectedFileRef.endLine}
          snippet={selectedFileRef.snippet}
          onClose={() => setSelectedFileRef(null)}
        />
      )}
    </div>
  );
};
