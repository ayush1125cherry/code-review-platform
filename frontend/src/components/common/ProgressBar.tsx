import React from 'react';

interface ProgressBarProps {
  progress: number;
  label?: string;
  stepText?: string;
  color?: 'brand' | 'green' | 'amber' | 'red';
}

export const ProgressBar: React.FC<ProgressBarProps> = ({
  progress,
  label,
  stepText,
  color = 'brand',
}) => {
  const colorMap = {
    brand: 'bg-gradient-to-r from-[#4f46e5] to-indigo-400',
    green: 'bg-emerald-500',
    amber: 'bg-amber-500',
    red: 'bg-rose-500',
  };

  return (
    <div className="w-full space-y-1.5">
      {(label || stepText) && (
        <div className="flex justify-between items-center text-xs">
          <span className="font-semibold text-slate-700">{stepText || label}</span>
          <span className="font-bold text-slate-500">{progress}%</span>
        </div>
      )}
      <div className="w-full bg-slate-200/80 rounded-full h-2 overflow-hidden border border-slate-200">
        <div
          className={`h-full rounded-full transition-all duration-500 ease-out ${colorMap[color]}`}
          style={{ width: `${Math.min(Math.max(progress, 0), 100)}%` }}
        />
      </div>
    </div>
  );
};
