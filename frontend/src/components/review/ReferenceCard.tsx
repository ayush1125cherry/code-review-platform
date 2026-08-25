import React from 'react';
import { CodeReference } from '../../types/review';
import { FileCode, ExternalLink, Hash } from 'lucide-react';

interface ReferenceCardProps {
  reference: CodeReference;
  onViewFile: (ref: CodeReference) => void;
}

export const ReferenceCard: React.FC<ReferenceCardProps> = ({ reference, onViewFile }) => {
  const fileName = reference.file || (reference.path.includes('/') ? reference.path.substring(reference.path.lastIndexOf('/') + 1) : reference.path);

  return (
    <div className="group relative bg-white hover:bg-slate-50 border border-slate-200/90 hover:border-indigo-300 rounded-xl p-3.5 transition-all duration-200 shadow-2xs flex flex-col justify-between space-y-3">
      <div>
        <div className="flex items-start justify-between space-x-2">
          <div className="flex items-center space-x-2 min-w-0">
            <div className="p-1.5 rounded-md bg-indigo-50 text-indigo-600 border border-indigo-100 shrink-0">
              <FileCode className="w-3.5 h-3.5" />
            </div>
            <h4 className="text-xs font-bold text-slate-800 truncate font-mono" title={fileName}>
              {fileName}
            </h4>
          </div>
          {reference.startLine && (
            <span className="text-[11px] font-mono font-medium px-2 py-0.5 rounded bg-slate-100 text-slate-700 border border-slate-200 shrink-0 flex items-center space-x-0.5">
              <Hash className="w-2.5 h-2.5 opacity-70" />
              <span>{reference.startLine}{reference.endLine && reference.endLine !== reference.startLine ? `-${reference.endLine}` : ''}</span>
            </span>
          )}
        </div>
        
        <p className="text-[11px] text-slate-500 font-mono truncate mt-1.5" title={reference.path}>
          {reference.path}
        </p>

        {reference.comment && (
          <p className="text-xs text-slate-600 mt-2 line-clamp-2 italic bg-slate-50 p-2 rounded border border-slate-200">
            "{reference.comment}"
          </p>
        )}
      </div>

      <button
        onClick={() => onViewFile(reference)}
        className="w-full mt-2 flex items-center justify-center space-x-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg bg-slate-100 hover:bg-[#4f46e5] text-slate-700 hover:text-white border border-slate-200 hover:border-indigo-600 transition-all duration-150"
      >
        <span>View File</span>
        <ExternalLink className="w-3 h-3" />
      </button>
    </div>
  );
};
