import React from 'react';
import { ReviewResponse } from '../../types/review';
import { Layers, Terminal, Sparkles, FolderGit2 } from 'lucide-react';

interface ReviewOverviewProps {
  review: ReviewResponse;
}

export const ReviewOverview: React.FC<ReviewOverviewProps> = ({ review }) => {
  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl p-6 space-y-5 shadow-xs">
      <div className="flex items-center space-x-3 pb-4 border-b border-slate-100">
        <div className="p-2 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100">
          <FolderGit2 className="w-5 h-5" />
        </div>
        <div>
          <span className="text-xs uppercase tracking-wider font-bold text-indigo-600">Repository Overview</span>
          <h3 className="text-lg font-bold text-slate-900">{review.repositoryType}</h3>
        </div>
      </div>

      <div className="space-y-4 text-sm leading-relaxed text-slate-600">
        <div>
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5 flex items-center space-x-1.5">
            <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
            <span>Executive Summary</span>
          </h4>
          <p className="bg-slate-50 p-4 rounded-xl border border-slate-200/80 text-slate-800 text-xs sm:text-sm leading-relaxed">
            {review.summary}
          </p>
        </div>

        {review.technologies && review.technologies.length > 0 && (
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2 flex items-center space-x-1.5">
              <Terminal className="w-3.5 h-3.5 text-indigo-600" />
              <span>Technologies Detected</span>
            </h4>
            <div className="flex flex-wrap gap-2">
              {review.technologies.map((tech, idx) => (
                <span
                  key={idx}
                  className="px-2.5 py-1 rounded-lg bg-slate-100 border border-slate-200 text-xs font-semibold text-slate-700"
                >
                  • {tech}
                </span>
              ))}
            </div>
          </div>
        )}

        {review.architecture && (
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5 flex items-center space-x-1.5">
              <Layers className="w-3.5 h-3.5 text-emerald-600" />
              <span>Architecture & Patterns</span>
            </h4>
            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200/80 text-slate-800 text-xs sm:text-sm whitespace-pre-line leading-relaxed">
              {review.architecture}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
