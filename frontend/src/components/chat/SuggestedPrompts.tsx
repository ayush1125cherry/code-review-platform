import React from 'react';

interface SuggestedPromptsProps {
  onSelectPrompt: (prompt: string) => void;
  efficiencyScore?: number;
}

export const SuggestedPrompts: React.FC<SuggestedPromptsProps> = ({ onSelectPrompt, efficiencyScore }) => {
  const prompts = [
    efficiencyScore ? `Why did you give the efficiency score ${efficiencyScore}?` : 'Why is this code inefficient?',
    'Which file has the biggest problem?',
    'Explain the security risks in detail.',
    'Show me the most problematic method to refactor.',
    'Is this project production ready?',
    'Explain this project to me like I am a beginner.',
  ];

  return (
    <div className="space-y-2 p-3 bg-slate-50/80 border border-slate-200 rounded-xl">
      <div className="text-xs font-bold text-slate-500">
        <span>Suggested Questions</span>
      </div>
      <div className="flex flex-wrap gap-1.5">
        {prompts.map((p, idx) => (
          <button
            key={idx}
            onClick={() => onSelectPrompt(p)}
            className="text-left text-xs px-2.5 py-1.5 rounded-lg bg-white hover:bg-indigo-50 text-slate-700 hover:text-indigo-700 border border-slate-200 hover:border-indigo-200 transition-colors shadow-2xs font-medium"
          >
            {p}
          </button>
        ))}
      </div>
    </div>
  );
};
