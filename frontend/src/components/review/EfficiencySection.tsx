import React from 'react';
import { CategoryReview, CodeReference } from '../../types/review';
import { ReferenceCard } from './ReferenceCard';
import { Cpu, Zap, AlertCircle, CheckCircle } from 'lucide-react';

interface EfficiencySectionProps {
  efficiency: CategoryReview;
  onViewFile: (ref: CodeReference) => void;
}

export const EfficiencySection: React.FC<EfficiencySectionProps> = ({ efficiency, onViewFile }) => {
  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl p-6 space-y-6 shadow-xs">
      <div className="flex items-center justify-between pb-4 border-b border-slate-100">
        <div className="flex items-center space-x-3">
          <div className="p-2 rounded-xl bg-amber-50 text-amber-600 border border-amber-100">
            <Cpu className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xs uppercase tracking-wider font-bold text-amber-600">Performance & Scaling</span>
            <h3 className="text-lg font-bold text-slate-900">Code Efficiency Analysis</h3>
          </div>
        </div>
        <div className="flex items-center space-x-2">
          <span className="text-xs text-slate-500 font-medium">Efficiency Score:</span>
          <span className="px-3 py-1 rounded-full text-xs font-bold font-mono bg-amber-50 text-amber-700 border border-amber-200">
            {efficiency.score} / 100
          </span>
        </div>
      </div>

      {efficiency.summary && (
        <div className="p-4 rounded-xl bg-slate-50 border border-slate-200/80 text-xs sm:text-sm text-slate-700 leading-relaxed">
          {efficiency.summary}
        </div>
      )}

      {efficiency.findings && efficiency.findings.length > 0 && (
        <div className="space-y-4">
          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center space-x-1.5">
            <Zap className="w-3.5 h-3.5 text-amber-600" />
            <span>Key Efficiency Observations ({efficiency.findings.length})</span>
          </h4>

          <div className="space-y-4">
            {efficiency.findings.map((finding, idx) => (
              <div
                key={idx}
                className="bg-slate-50/70 border border-slate-200/80 rounded-xl p-5 space-y-3.5 transition-all hover:border-slate-300"
              >
                <div className="flex items-start justify-between gap-2">
                  <h5 className="text-sm font-bold text-slate-900">{finding.title}</h5>
                </div>

                {/* Evidence Box */}
                <div className="p-3.5 rounded-lg bg-amber-50/70 border-l-4 border-amber-500 text-xs text-slate-800 space-y-1">
                  <div className="flex items-center space-x-1.5 text-amber-800 font-bold text-[11px] uppercase tracking-wide">
                    <AlertCircle className="w-3.5 h-3.5" />
                    <span>Evidence</span>
                  </div>
                  <p className="leading-relaxed pl-5 text-slate-700">{finding.description}</p>
                </div>

                {/* Recommendation Box */}
                {finding.recommendation && (
                  <div className="p-3.5 rounded-lg bg-emerald-50/70 border-l-4 border-emerald-500 text-xs text-slate-800 space-y-1">
                    <div className="flex items-center space-x-1.5 text-emerald-800 font-bold text-[11px] uppercase tracking-wide">
                      <CheckCircle className="w-3.5 h-3.5" />
                      <span>Recommendation</span>
                    </div>
                    <p className="leading-relaxed pl-5 text-slate-700">{finding.recommendation}</p>
                  </div>
                )}

                {/* Reference Cards */}
                {finding.references && finding.references.length > 0 && (
                  <div className="space-y-2 pt-1">
                    <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500">
                      Referenced Code:
                    </span>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                      {finding.references.map((ref, rIdx) => (
                        <ReferenceCard key={rIdx} reference={ref} onViewFile={onViewFile} />
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
