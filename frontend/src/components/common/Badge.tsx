import React from 'react';
import { FindingSeverity } from '../../types/review';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info' | 'purple' | 'gray';
  severity?: FindingSeverity;
  size?: 'sm' | 'md';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant,
  severity,
  size = 'sm',
  className = '',
}) => {
  let resolvedVariant = variant || 'default';

  if (severity) {
    switch (severity) {
      case 'CRITICAL':
      case 'HIGH':
        resolvedVariant = 'danger';
        break;
      case 'MEDIUM':
        resolvedVariant = 'warning';
        break;
      case 'LOW':
        resolvedVariant = 'info';
        break;
      case 'INFO':
      default:
        resolvedVariant = 'gray';
        break;
    }
  }

  const variantClasses = {
    default: 'bg-indigo-50 text-indigo-700 border-indigo-200',
    success: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    warning: 'bg-amber-50 text-amber-700 border-amber-200',
    danger: 'bg-rose-50 text-rose-700 border-rose-200',
    info: 'bg-sky-50 text-sky-700 border-sky-200',
    purple: 'bg-purple-50 text-purple-700 border-purple-200',
    gray: 'bg-slate-100 text-slate-700 border-slate-200',
  };

  const sizeClasses = {
    sm: 'text-[11px] px-2 py-0.5',
    md: 'text-xs px-2.5 py-1',
  };

  return (
    <span className={`inline-flex items-center font-bold rounded-md border ${variantClasses[resolvedVariant]} ${sizeClasses[size]} ${className}`}>
      {children}
    </span>
  );
};
