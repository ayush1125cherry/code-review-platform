import React from 'react';
import { ScoreBreakdown } from '../../types/review';
import { ScoreGauge } from './ScoreGauge';
import { ShieldCheck, Cpu, Code2, Layers, Wrench, FileCheck, BookOpen } from 'lucide-react';

interface ScoreCardProps {
  scores: ScoreBreakdown;
}

export const ScoreCard: React.FC<ScoreCardProps> = ({ scores }) => {
  const categories = [
    { key: 'architecture', label: 'Architecture', score: scores.architecture, icon: Layers },
    { key: 'codeQuality', label: 'Code Quality', score: scores.codeQuality, icon: Code2 },
    { key: 'efficiency', label: 'Efficiency', score: scores.efficiency, icon: Cpu },
    { key: 'security', label: 'Security', score: scores.security, icon: ShieldCheck },
    { key: 'maintainability', label: 'Maintainability', score: scores.maintainability, icon: Wrench },
    { key: 'testing', label: 'Testing', score: scores.testing, icon: FileCheck },
    { key: 'documentation', label: 'Documentation', score: scores.documentation, icon: BookOpen },
  ];

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    if (score >= 65) return 'bg-indigo-50 text-indigo-700 border-indigo-200';
    if (score >= 50) return 'bg-amber-50 text-amber-700 border-amber-200';
    return 'bg-rose-50 text-rose-700 border-rose-200';
  };

  const getBarColor = (score: number) => {
    if (score >= 80) return 'bg-emerald-500';
    if (score >= 65) return 'bg-indigo-600';
    if (score >= 50) return 'bg-amber-500';
    return 'bg-rose-500';
  };

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl p-6 shadow-xs space-y-6">
      <div className="flex flex-col sm:flex-row items-center sm:items-start justify-between gap-6 pb-6 border-b border-slate-100">
        <div>
          <h2 className="text-xl font-bold text-slate-900">Repository Score</h2>
          <p className="text-xs text-slate-500 mt-1 max-w-sm">
            Comprehensive algorithmic & architectural evaluation computed across 7 quality dimensions.
          </p>
        </div>
        <div className="shrink-0">
          <ScoreGauge score={scores.overall} />
        </div>
      </div>

      <div className="space-y-3.5">
        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Score Breakdown</h3>
        <div className="grid grid-cols-1 gap-2.5">
          {categories.map(({ key, label, score, icon: Icon }) => (
            <div key={key} className="flex items-center justify-between p-3 rounded-xl bg-slate-50/70 border border-slate-200/70 hover:border-slate-300 transition-colors">
              <div className="flex items-center space-x-3 w-40">
                <div className="p-1.5 rounded-lg bg-white text-slate-700 border border-slate-200 shadow-2xs">
                  <Icon className="w-3.5 h-3.5" />
                </div>
                <span className="text-xs font-bold text-slate-800">{label}</span>
              </div>

              {/* Progress Bar */}
              <div className="flex-1 mx-4 hidden sm:block">
                <div className="w-full bg-slate-200/80 rounded-full h-2 overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-700 ${getBarColor(score)}`}
                    style={{ width: `${score}%` }}
                  />
                </div>
              </div>

              {/* Score pill */}
              <div className={`px-2.5 py-0.5 rounded-full font-mono text-xs font-bold border ${getScoreColor(score)}`}>
                {score}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
