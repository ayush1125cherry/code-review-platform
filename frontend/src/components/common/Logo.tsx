import React from 'react';

interface LogoProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  showText?: boolean;
  className?: string;
  iconClassName?: string;
}

export const CodeReviewLogoIcon: React.FC<{ className?: string }> = ({ className = 'w-9 h-9' }) => (
  <svg
    viewBox="0 0 100 100"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    className={`${className} shrink-0 select-none drop-shadow-md`}
  >
    <defs>
      <linearGradient id="cr-blueGrad" x1="10%" y1="0%" x2="90%" y2="100%">
        <stop offset="0%" stopColor="#22D3EE" />
        <stop offset="45%" stopColor="#2563EB" />
        <stop offset="100%" stopColor="#7C3AED" />
      </linearGradient>

      <linearGradient id="cr-greenGrad" x1="20%" y1="10%" x2="80%" y2="90%">
        <stop offset="0%" stopColor="#4ADE80" />
        <stop offset="100%" stopColor="#16A34A" />
      </linearGradient>

      <linearGradient id="cr-codeGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stopColor="#38BDF8" />
        <stop offset="100%" stopColor="#6366F1" />
      </linearGradient>

      <radialGradient id="cr-glassGrad" cx="35%" cy="25%" r="80%">
        <stop offset="0%" stopColor="#60A5FA" stopOpacity="0.8" />
        <stop offset="55%" stopColor="#2563EB" stopOpacity="0.45" />
        <stop offset="100%" stopColor="#1E3A8A" stopOpacity="0.9" />
      </radialGradient>

      <filter id="cr-glow" x="-50%" y="-50%" width="200%" height="200%">
        <feGaussianBlur stdDeviation="1.5" result="blur" />
        <feMerge>
          <feMergeNode in="blur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>

      <filter id="cr-shadow" x="-30%" y="-30%" width="160%" height="160%">
        <feDropShadow dx="0" dy="2" stdDeviation="2" floodOpacity="0.35" />
      </filter>
    </defs>

    {/* Back rounded-square layer */}
    <rect
      x="15"
      y="12"
      width="70"
      height="70"
      rx="15"
      stroke="url(#cr-blueGrad)"
      strokeWidth="3"
      opacity="0.9"
    />

    {/* Main code editor */}
    <rect
      x="19"
      y="20"
      width="62"
      height="58"
      rx="10"
      fill="#101A3A"
      stroke="url(#cr-blueGrad)"
      strokeWidth="2.2"
      filter="url(#cr-shadow)"
    />

    {/* Editor top bar */}
    <circle cx="27" cy="28" r="2.2" fill="#EF4444" />
    <circle cx="34" cy="28" r="2.2" fill="#FACC15" />
    <circle cx="41" cy="28" r="2.2" fill="#22C55E" />

    {/* Code lines */}
    <rect x="27" y="36" width="25" height="2.8" rx="1.4" fill="#22D3EE" />
    <rect x="55" y="36" width="13" height="2.8" rx="1.4" fill="#8B5CF6" />

    {/* Code brackets */}
    <path
      d="M38 42 L32 48 L38 54"
      stroke="#38BDF8"
      strokeWidth="3"
      strokeLinecap="round"
      strokeLinejoin="round"
      filter="url(#cr-glow)"
    />

    <path
      d="M46 42 L52 48 L46 54"
      stroke="#22D3EE"
      strokeWidth="3"
      strokeLinecap="round"
      strokeLinejoin="round"
      filter="url(#cr-glow)"
    />

    {/* More code */}
    <rect x="27" y="59" width="19" height="2.8" rx="1.4" fill="#2563EB" />
    <rect x="49" y="59" width="14" height="2.8" rx="1.4" fill="#FACC15" />

    <rect x="27" y="65" width="28" height="2.8" rx="1.4" fill="#6366F1" />
    <rect x="58" y="65" width="9" height="2.8" rx="1.4" fill="#22D3EE" />

    {/* Magnifying glass */}
    <circle
      cx="65"
      cy="52"
      r="15"
      fill="url(#cr-glassGrad)"
      stroke="url(#cr-blueGrad)"
      strokeWidth="3"
      filter="url(#cr-shadow)"
    />

    {/* Glass reflection */}
    <path
      d="M56 44 C59 40 64 39 68 40"
      stroke="#FFFFFF"
      strokeWidth="2"
      strokeLinecap="round"
      opacity="0.75"
    />

    {/* Magnifying glass handle */}
    <path
      d="M75 63 L86 76"
      stroke="#2563EB"
      strokeWidth="7"
      strokeLinecap="round"
    />

    <path
      d="M75 63 L86 76"
      stroke="#60A5FA"
      strokeWidth="2"
      strokeLinecap="round"
      opacity="0.7"
    />

    {/* Check badge */}
    <circle
      cx="38"
      cy="73"
      r="11"
      fill="url(#cr-greenGrad)"
      stroke="#22C55E"
      strokeWidth="2"
      filter="url(#cr-shadow)"
    />

    {/* Checkmark */}
    <path
      d="M32 73 L36 77 L44 68"
      stroke="#FFFFFF"
      strokeWidth="3.2"
      strokeLinecap="round"
      strokeLinejoin="round"
    />

    {/* Small decorative glow */}
    <circle cx="80" cy="25" r="1.8" fill="#38BDF8" opacity="0.9" />
    <circle cx="22" cy="80" r="1.3" fill="#8B5CF6" opacity="0.8" />
    <circle cx="78" cy="80" r="1" fill="#22D3EE" opacity="0.7" />
  </svg>
);

export const Logo: React.FC<LogoProps> = ({
  size = 'md',
  showText = true,
  className = '',
  iconClassName = '',
}) => {
  const iconSizes = {
    sm: 'w-7 h-7',
    md: 'w-9 h-9',
    lg: 'w-14 h-14',
    xl: 'w-20 h-20',
  };

  return (
    <div className={`flex items-center space-x-3 ${className}`}>
      <CodeReviewLogoIcon className={`${iconSizes[size]} ${iconClassName}`} />

      {showText && (
        <div className="min-w-0">
          <h1 className="font-extrabold text-sm text-slate-900 tracking-tight leading-tight">
            CodeReview AI
          </h1>
          <p className="text-[10px] text-slate-500 font-medium tracking-tight leading-none mt-0.5">
            GitHub Repository Reviewer
          </p>
        </div>
      )}
    </div>
  );
};
