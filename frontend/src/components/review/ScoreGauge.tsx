import React from 'react';

interface ScoreGaugeProps {
  score: number;
  size?: number;
  strokeWidth?: number;
}

export const ScoreGauge: React.FC<ScoreGaugeProps> = ({
  score,
  size = 140,
  strokeWidth = 10,
}) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (score / 100) * circumference;

  const getColor = (s: number) => {
    if (s >= 80) return '#10b981'; // emerald
    if (s >= 65) return '#4f46e5'; // indigo
    if (s >= 50) return '#f59e0b'; // amber
    return '#f43f5e'; // rose
  };

  const getLabel = (s: number) => {
    if (s >= 85) return 'Excellent';
    if (s >= 75) return 'Good';
    if (s >= 60) return 'Average';
    if (s >= 40) return 'Needs Work';
    return 'Critical';
  };

  const color = getColor(score);

  return (
    <div className="relative flex flex-col items-center justify-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="transform -rotate-90">
        {/* Background track */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="#e2e8f0"
          strokeWidth={strokeWidth}
          fill="transparent"
        />
        {/* Animated progress track */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke={color}
          strokeWidth={strokeWidth}
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          fill="transparent"
          className="transition-all duration-1000 ease-out"
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
        <span className="text-3xl font-extrabold text-slate-900 tracking-tight">{score}</span>
        <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 -mt-0.5">/ 100</span>
        <span className="text-[10px] font-semibold px-2 py-0.5 mt-1 rounded-full bg-slate-100 text-slate-700 border border-slate-200">
          {getLabel(score)}
        </span>
      </div>
    </div>
  );
};
