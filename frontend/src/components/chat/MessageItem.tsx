import React from 'react';
import { Message } from '../../types/chat';
import { CodeReference } from '../../types/review';
import { ReferenceChips } from './ReferenceChips';
import { CodeReviewLogoIcon } from '../common/Logo';
import { User as UserIcon } from 'lucide-react';

interface MessageItemProps {
  message: Message;
  onViewFile: (ref: CodeReference) => void;
}

export const MessageItem: React.FC<MessageItemProps> = ({ message, onViewFile }) => {
  const isUser = message.sender === 'USER';

  // Format message text: convert bold, code blocks, list items cleanly
  const renderFormattedContent = (content: string) => {
    return content.split('\n\n').map((paragraph, pIdx) => {
      // Check if it's a code block
      if (paragraph.startsWith('```')) {
        const lines = paragraph.split('\n');
        const codeLines = lines.slice(1, lines.length - 1).join('\n');
        return (
          <div key={pIdx} className="my-2 rounded-lg bg-slate-900 text-slate-100 p-3 font-mono text-xs overflow-x-auto border border-slate-800">
            <pre>{codeLines || lines.join('\n')}</pre>
          </div>
        );
      }

      // Check if list
      if (paragraph.startsWith('- ') || paragraph.startsWith('• ') || paragraph.startsWith('1. ')) {
        const items = paragraph.split('\n');
        return (
          <ul key={pIdx} className="space-y-1 my-1.5 list-disc pl-4 text-xs sm:text-sm">
            {items.map((item, iIdx) => {
              const cleanItem = item.replace(/^[-•*]\s+|\d+\.\s+/, '');
              return <li key={iIdx}>{formatInline(cleanItem)}</li>;
            })}
          </ul>
        );
      }

      return (
        <p key={pIdx} className="my-1 text-xs sm:text-sm leading-relaxed whitespace-pre-line">
          {formatInline(paragraph)}
        </p>
      );
    });
  };

  const formatInline = (text: string) => {
    // Basic inline markdown handling for bold **text** and backticks `code`
    const parts = text.split(/(\*\*.*?\*\*|`.*?`)/g);
    return parts.map((part, idx) => {
      if (part.startsWith('**') && part.endsWith('**')) {
        return <strong key={idx} className="font-bold">{part.slice(2, -2)}</strong>;
      }
      if (part.startsWith('`') && part.endsWith('`')) {
        return <code key={idx} className="px-1.5 py-0.5 rounded bg-slate-100 text-indigo-700 font-mono text-[11px] border border-slate-200">{part.slice(1, -1)}</code>;
      }
      return part;
    });
  };

  return (
    <div className={`flex items-start space-x-3 py-3 ${isUser ? 'justify-end' : 'justify-start'}`}>
      {!isUser && (
        <div className="w-8 h-8 rounded-xl flex items-center justify-center shrink-0">
          <CodeReviewLogoIcon className="w-8 h-8" />
        </div>
      )}

      <div
        className={`max-w-[85%] sm:max-w-[80%] rounded-2xl p-4 transition-all shadow-xs ${
          isUser
            ? 'bg-[#4f46e5] text-white rounded-tr-none'
            : 'bg-white border border-slate-200/90 text-slate-800 rounded-tl-none'
        }`}
      >
        <div className="text-[11px] font-bold mb-1 opacity-70">
          {isUser ? 'You' : 'CodeReview AI'}
        </div>

        <div className="space-y-1">
          {renderFormattedContent(message.content)}
        </div>

        {!isUser && message.references && message.references.length > 0 && (
          <ReferenceChips references={message.references} onViewFile={onViewFile} />
        )}
      </div>

      {isUser && (
        <div className="w-8 h-8 rounded-xl bg-slate-200 flex items-center justify-center text-slate-700 shrink-0 border border-slate-300">
          <UserIcon className="w-4 h-4" />
        </div>
      )}
    </div>
  );
};
