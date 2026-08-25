import React, { useEffect, useRef, useState } from 'react';
import { X, Copy, Check, FileCode } from 'lucide-react';
import { repoService } from '../../services/repoService';
import { LoadingSpinner } from '../common/LoadingSpinner';

interface CodeViewerModalProps {
  repositoryId: number;
  filePath: string;
  startLine?: number;
  endLine?: number;
  snippet?: string;
  onClose: () => void;
}

export const CodeViewerModal: React.FC<CodeViewerModalProps> = ({
  repositoryId,
  filePath,
  startLine,
  endLine,
  snippet,
  onClose,
}) => {
  const [content, setContent] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [copied, setCopied] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const highlightedRef = useRef<HTMLDivElement | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    let isMounted = true;
    const fetchFile = async () => {
      setLoading(true);
      setError(null);
      try {
        const file = await repoService.getRepositoryFile(repositoryId, filePath);
        if (isMounted) {
          setContent(file.content || '');
        }
      } catch (err: any) {
        if (isMounted) {
          if (snippet) {
            setContent(snippet);
          } else {
            setError(err.response?.data?.message || 'Failed to load file content');
          }
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchFile();
    return () => {
      isMounted = false;
    };
  }, [repositoryId, filePath, snippet]);

  useEffect(() => {
    if (!loading && highlightedRef.current && containerRef.current) {
      setTimeout(() => {
        highlightedRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 100);
    }
  }, [loading, startLine]);

  const handleCopy = () => {
    if (content) {
      navigator.clipboard.writeText(content);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const lines = content.split('\n');
  const fileName = filePath.includes('/') ? filePath.substring(filePath.lastIndexOf('/') + 1) : filePath;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-white border border-slate-200 rounded-2xl w-full max-w-5xl h-[85vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50">
          <div className="flex items-center space-x-3 overflow-hidden">
            <div className="p-2 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100 shrink-0">
              <FileCode className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <div className="flex items-center space-x-2">
                <h3 className="font-bold text-slate-900 truncate text-sm sm:text-base">{fileName}</h3>
                {startLine && (
                  <span className="text-xs px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 font-mono font-bold border border-indigo-200">
                    Lines {startLine}{endLine && endLine !== startLine ? `–${endLine}` : ''}
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-500 font-mono truncate">{filePath}</p>
            </div>
          </div>

          <div className="flex items-center space-x-2 shrink-0">
            <button
              onClick={handleCopy}
              className="flex items-center space-x-1.5 px-3 py-1.5 text-xs rounded-xl bg-white hover:bg-slate-100 text-slate-700 font-semibold transition-colors border border-slate-200 shadow-2xs"
              title="Copy code"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? 'Copied' : 'Copy'}</span>
            </button>
            <button
              onClick={onClose}
              className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Code Content Container */}
        <div ref={containerRef} className="flex-1 overflow-auto p-4 bg-[#0d1117] font-mono text-xs sm:text-sm leading-relaxed select-text text-slate-200">
          {loading ? (
            <div className="flex flex-col items-center justify-center h-full space-y-3">
              <LoadingSpinner size="lg" />
              <p className="text-sm text-slate-400">Loading file contents...</p>
            </div>
          ) : error ? (
            <div className="flex flex-col items-center justify-center h-full text-rose-400 space-y-2">
              <p className="font-medium">{error}</p>
            </div>
          ) : (
            <div className="table w-full border-collapse">
              {lines.map((lineText, idx) => {
                const lineNum = idx + 1;
                const isTarget = startLine && endLine
                  ? lineNum >= startLine && lineNum <= endLine
                  : startLine ? lineNum === startLine : false;
                const isFirstTarget = startLine ? lineNum === startLine : false;

                return (
                  <div
                    key={lineNum}
                    ref={isFirstTarget ? highlightedRef : null}
                    className={`table-row transition-colors ${
                      isTarget
                        ? 'bg-indigo-950/60 border-l-4 border-indigo-500 text-white font-semibold'
                        : 'hover:bg-slate-800/40 text-slate-300'
                    }`}
                  >
                    {/* Line number */}
                    <span className="table-cell pr-4 pl-2 py-0.5 text-right select-none text-slate-600 font-mono text-xs w-12 border-r border-slate-800">
                      {lineNum}
                    </span>
                    {/* Code line */}
                    <span className="table-cell pl-4 pr-2 py-0.5 whitespace-pre font-mono">
                      {lineText || ' '}
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer info */}
        <div className="px-6 py-2.5 bg-slate-50 border-t border-slate-200 flex justify-between items-center text-xs text-slate-500">
          <span>Total Lines: {lines.length}</span>
          <span>{startLine ? `Focus: L${startLine}${endLine ? `-${endLine}` : ''}` : 'Read only'}</span>
        </div>
      </div>
    </div>
  );
};
