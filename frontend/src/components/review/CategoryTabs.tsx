import React, { useState } from 'react';
import { CategoryReview, CodeReference } from '../../types/review';
import { ReferenceCard } from './ReferenceCard';
import { Shield, Wrench, CheckCircle, FileText } from 'lucide-react';

interface CategoryTabsProps {
  security: CategoryReview;
  maintainability: CategoryReview;
  testing: CategoryReview;
  documentation: CategoryReview;
  onViewFile: (ref: CodeReference) => void;
}

export const CategoryTabs: React.FC<CategoryTabsProps> = ({
  security,
  maintainability,
  testing,
  documentation,
  onViewFile,
}) => {
  const [activeTab, setActiveTab] = useState<'security' | 'maintainability' | 'testing' | 'documentation'>('security');

  const tabs = [
    { key: 'security', label: 'Security', score: security.score, icon: Shield, data: security },
    { key: 'maintainability', label: 'Maintainability', score: maintainability.score, icon: Wrench, data: maintainability },
    { key: 'testing', label: 'Testing', score: testing.score, icon: CheckCircle, data: testing },
    { key: 'documentation', label: 'Documentation', score: documentation.score, icon: FileText, data: documentation },
  ];

  const currentTab = tabs.find(t => t.key === activeTab)!;

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl p-6 space-y-6 shadow-xs">
      {/* Tab Navigation */}
      <div className="flex flex-wrap gap-2 pb-4 border-b border-slate-100">
        {tabs.map(({ key, label, score, icon: Icon }) => (
          <button
            key={key}
            onClick={() => setActiveTab(key as any)}
            className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
              activeTab === key
                ? 'bg-[#4f46e5] text-white shadow-xs'
                : 'bg-slate-100 text-slate-600 hover:text-slate-900 hover:bg-slate-200/70 border border-slate-200/80'
            }`}
          >
            <Icon className="w-3.5 h-3.5" />
            <span>{label}</span>
            <span className={`px-1.5 py-0.5 rounded-full text-[10px] font-mono font-bold ${activeTab === key ? 'bg-white/20 text-white' : 'bg-white text-slate-700 border border-slate-200'}`}>
              {score}
            </span>
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="space-y-4">
        {currentTab.data.summary && (
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200/80 text-xs sm:text-sm text-slate-700 leading-relaxed">
            {currentTab.data.summary}
          </div>
        )}

        {currentTab.data.findings && currentTab.data.findings.length > 0 ? (
          <div className="space-y-3">
            {currentTab.data.findings.map((f, idx) => (
              <div key={idx} className="bg-slate-50/70 border border-slate-200/80 rounded-xl p-5 space-y-2.5">
                <h5 className="text-xs sm:text-sm font-bold text-slate-900">{f.title}</h5>
                <p className="text-xs text-slate-600 leading-relaxed">{f.description}</p>
                {f.recommendation && (
                  <p className="text-xs text-indigo-900 bg-indigo-50/70 p-3 rounded-lg border border-indigo-200">
                    <span className="font-bold text-indigo-800">Recommendation:</span> {f.recommendation}
                  </p>
                )}
                {f.references && f.references.length > 0 && (
                  <div className="pt-1 grid grid-cols-1 sm:grid-cols-2 gap-2">
                    {f.references.map((ref, rIdx) => (
                      <ReferenceCard key={rIdx} reference={ref} onViewFile={onViewFile} />
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <p className="text-xs text-slate-400 italic">No specific critical findings recorded for this category.</p>
        )}
      </div>
    </div>
  );
};
