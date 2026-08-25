import React from 'react';
import { CodeReference } from '../../types/review';
import { FileCode, ArrowUpRight } from 'lucide-react';

interface ReferenceChipsProps {
  references: CodeReference[];
  onViewFile: (ref: CodeReference) => void;
}

export const ReferenceChips: React.FC<ReferenceChipsProps> = ({ references, onViewFile }) => {
  if (!references || references.length === 0) return null;

  return (
    <div className="space-y-1.5 mt-2.5 pt-2 border-t border-slate-200">
      <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
        Referenced Files:
      </span>
      <div className="flex flex-wrap gap-1.5">
        {references.map((ref, idx) => {
          const fileName = ref.file || (ref.path.includes('/') ? ref.path.substring(ref.path.lastIndexOf('/') + 1) : ref.path);
          return (
            <button
              key={idx}
              onClick={() => onViewFile(ref)}
              className="group inline-flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-slate-50 hover:bg-indigo-50 text-slate-700 hover:text-indigo-700 border border-slate-200 hover:border-indigo-200 text-xs transition-colors font-mono shadow-2xs"
            >
              <FileCode className="w-3 h-3 text-indigo-600 group-hover:text-indigo-700" />
              <span className="font-semibold truncate max-w-[150px]">{fileName}</span>
              {ref.startLine && (
                <span className="text-[10px] text-indigo-600 opacity-80">
                  :{ref.startLine}{ref.endLine && ref.endLine !== ref.startLine ? `-${ref.endLine}` : ''}
                </span>
              )}
              <ArrowUpRight className="w-2.5 h-2.5 opacity-60 group-hover:opacity-100" />
            </button>
          );
        })}
      </div>
    </div>
  );
};
