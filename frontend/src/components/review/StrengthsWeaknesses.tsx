import React from 'react';
import { CodeReference, ReviewFinding } from '../../types/review';
import { ReferenceCard } from './ReferenceCard';
import { Badge } from '../common/Badge';
import { ThumbsUp, AlertTriangle, CheckCircle2, ShieldAlert } from 'lucide-react';

interface StrengthsWeaknessesProps {
  strengths: ReviewFinding[];
  weaknesses: ReviewFinding[];
  onViewFile: (ref: CodeReference) => void;
}

export const StrengthsWeaknesses: React.FC<StrengthsWeaknessesProps> = ({
  strengths,
  weaknesses,
  onViewFile,
}) => {
  return (
    <div className="space-y-6">
      {/* Strengths / What's Good */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-6 space-y-5 shadow-xs">
        <div className="flex items-center space-x-3 pb-4 border-b border-slate-100">
          <div className="p-2 rounded-xl bg-emerald-50 text-emerald-600 border border-emerald-100">
            <ThumbsUp className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs uppercase tracking-wider font-bold text-emerald-600">Architectural Strengths</span>
            <h3 className="text-lg font-bold text-slate-900">What's Good in This Codebase</h3>
          </div>
        </div>

        <div className="space-y-4">
          {strengths.map((item, idx) => (
            <div
              key={idx}
              className="bg-slate-50/70 border border-slate-200/80 hover:border-emerald-300 rounded-xl p-5 space-y-3 transition-colors"
            >
              <div className="flex items-center space-x-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                <h4 className="text-sm font-bold text-slate-900">{item.title}</h4>
              </div>

              <p className="text-xs sm:text-sm text-slate-600 leading-relaxed pl-6">
                {item.description}
              </p>

              {item.references && item.references.length > 0 && (
                <div className="pl-6 pt-1 space-y-2">
                  <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500">
                    Relevant Files:
                  </span>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    {item.references.map((ref, rIdx) => (
                      <ReferenceCard key={rIdx} reference={ref} onViewFile={onViewFile} />
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Weaknesses / Areas for Improvement */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-6 space-y-5 shadow-xs">
        <div className="flex items-center space-x-3 pb-4 border-b border-slate-100">
          <div className="p-2 rounded-xl bg-rose-50 text-rose-600 border border-rose-100">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs uppercase tracking-wider font-bold text-rose-600">Actionable Weaknesses</span>
            <h3 className="text-lg font-bold text-slate-900">Areas for Improvement & Risks</h3>
          </div>
        </div>

        <div className="space-y-4">
          {weaknesses.map((item, idx) => (
            <div
              key={idx}
              className="bg-slate-50/70 border border-slate-200/80 hover:border-rose-300 rounded-xl p-5 space-y-3 transition-colors"
            >
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center space-x-2">
                  <ShieldAlert className="w-4 h-4 text-rose-600 shrink-0" />
                  <h4 className="text-sm font-bold text-slate-900">{item.title}</h4>
                </div>
                <Badge severity={item.severity}>
                  {item.severity} SEVERITY
                </Badge>
              </div>

              {/* Evidence */}
              <div className="p-3.5 rounded-lg bg-rose-50/70 border-l-4 border-rose-500 text-xs text-slate-800 space-y-1">
                <span className="text-[11px] font-bold uppercase tracking-wider text-rose-800 block">
                  Evidence:
                </span>
                <p className="leading-relaxed text-slate-700">{item.description}</p>
              </div>

              {/* Recommendation */}
              {item.recommendation && (
                <div className="p-3.5 rounded-lg bg-indigo-50/70 border-l-4 border-indigo-500 text-xs text-slate-800 space-y-1">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-indigo-800 block">
                    Recommendation:
                  </span>
                  <p className="leading-relaxed text-slate-700">{item.recommendation}</p>
                </div>
              )}

              {item.references && item.references.length > 0 && (
                <div className="pt-1 space-y-2">
                  <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500">
                    Relevant Files:
                  </span>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    {item.references.map((ref, rIdx) => (
                      <ReferenceCard key={rIdx} reference={ref} onViewFile={onViewFile} />
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
