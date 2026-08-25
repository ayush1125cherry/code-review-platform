import React, { useState, useEffect, useRef } from 'react';
import { Message } from '../../types/chat';
import { CodeReference } from '../../types/review';
import { chatService } from '../../services/chatService';
import { MessageItem } from './MessageItem';
import { SuggestedPrompts } from './SuggestedPrompts';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { Send, MessageSquare, Sparkles } from 'lucide-react';

interface ChatInterfaceProps {
  reviewId: number;
  initialConversationId?: number;
  efficiencyScore?: number;
  onViewFile: (ref: CodeReference) => void;
}

export const ChatInterface: React.FC<ChatInterfaceProps> = ({
  reviewId,
  initialConversationId,
  efficiencyScore,
  onViewFile,
}) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [conversationId, setConversationId] = useState<number | undefined>(initialConversationId);
  const [inputText, setInputText] = useState<string>('');
  const [isSending, setIsSending] = useState<boolean>(false);
  const [loadingHistory, setLoadingHistory] = useState<boolean>(true);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadConversation = async () => {
      setLoadingHistory(true);
      try {
        if (conversationId) {
          const conv = await chatService.getConversation(conversationId);
          if (isMounted && conv) {
            setMessages(conv.messages || []);
          }
        } else {
          const convList = await chatService.getConversationsByReview(reviewId);
          if (isMounted && convList.length > 0) {
            setConversationId(convList[0].id);
            setMessages(convList[0].messages || []);
          }
        }
      } catch (err) {
        console.error('Failed to load conversation history', err);
      } finally {
        if (isMounted) setLoadingHistory(false);
      }
    };

    loadConversation();
    return () => {
      isMounted = false;
    };
  }, [reviewId, conversationId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isSending]);

  const handleSend = async (textToSend?: string) => {
    const query = (textToSend || inputText).trim();
    if (!query || isSending) return;

    const optimisticMsg: Message = {
      sender: 'USER',
      content: query,
      references: [],
      createdAt: new Date().toISOString(),
    };

    setMessages(prev => [...prev, optimisticMsg]);
    setInputText('');
    setIsSending(true);

    try {
      const response = await chatService.sendMessage(reviewId, {
        message: query,
        conversationId: conversationId,
      });

      setConversationId(response.conversationId);
      setMessages(prev => [...prev, response.aiMessage]);
    } catch (err: any) {
      const errorMsg: Message = {
        sender: 'AI',
        content: `Error generating response: ${err?.response?.data?.message || err?.message || 'Please try again.'}`,
        references: [],
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setIsSending(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl flex flex-col h-[calc(100vh-8.5rem)] min-h-[450px] shadow-xs overflow-hidden">
      {/* Header */}
      <div className="px-6 py-4 border-b border-slate-100 bg-slate-50/70 flex items-center justify-between">
        <div className="flex items-center space-x-2.5">
          <div className="p-2 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100">
            <MessageSquare className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-bold text-slate-900 text-sm">Repository Chat</h3>
            <p className="text-[11px] text-slate-500">RAG-powered conversational AI with repository context</p>
          </div>
        </div>
        <div className="flex items-center space-x-1 text-xs text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
          <span className="font-semibold text-[11px]">Online</span>
        </div>
      </div>

      {/* Messages Stream */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-2 bg-[#f8fafc]">
        {loadingHistory ? (
          <div className="flex flex-col items-center justify-center h-full space-y-2 text-slate-400 text-xs">
            <LoadingSpinner size="md" />
            <span>Loading conversation...</span>
          </div>
        ) : (
          <>
            {messages.map((msg, idx) => (
              <MessageItem key={idx} message={msg} onViewFile={onViewFile} />
            ))}

            {isSending && (
              <div className="flex items-start space-x-3 py-4">
                <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 to-purple-600 flex items-center justify-center text-white shrink-0 shadow-sm">
                  <LoadingSpinner size="sm" className="border-white" />
                </div>
                <div className="bg-white border border-slate-200 rounded-2xl rounded-tl-none p-4 text-xs text-slate-500 flex items-center space-x-2 shadow-xs">
                  <Sparkles className="w-3.5 h-3.5 text-indigo-600 animate-spin" />
                  <span>Searching code chunks and formulating response...</span>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* Suggested Prompts (when message history is short) */}
      {messages.length <= 2 && (
        <div className="px-6 py-2 border-t border-slate-100 bg-white">
          <SuggestedPrompts onSelectPrompt={(p) => handleSend(p)} efficiencyScore={efficiencyScore} />
        </div>
      )}

      {/* Input Area */}
      <div className="p-4 border-t border-slate-100 bg-white">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          className="relative flex items-end bg-slate-50 border border-slate-200 rounded-xl focus-within:border-indigo-500 focus-within:ring-2 focus-within:ring-indigo-500/20 focus-within:bg-white transition-all p-2"
        >
          <textarea
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask anything about this repository, architecture, or code efficiency..."
            rows={2}
            className="w-full bg-transparent text-slate-900 text-xs sm:text-sm placeholder-slate-400 focus:outline-none resize-none px-2 py-1"
          />

          <button
            type="submit"
            disabled={!inputText.trim() || isSending}
            className="p-2 rounded-lg bg-[#4f46e5] hover:bg-indigo-700 disabled:opacity-40 disabled:hover:bg-[#4f46e5] text-white transition-colors shrink-0 mb-0.5 ml-2"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
        <p className="text-[10px] text-slate-400 mt-2 text-center">
          Press <kbd className="px-1 py-0.5 rounded bg-slate-100 border border-slate-200 font-mono text-slate-600">Enter</kbd> to send, <kbd className="px-1 py-0.5 rounded bg-slate-100 border border-slate-200 font-mono text-slate-600">Shift+Enter</kbd> for new line
        </p>
      </div>
    </div>
  );
};
