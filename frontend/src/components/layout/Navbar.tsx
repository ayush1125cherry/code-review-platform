import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { useUI } from '../../context/UIContext';
import { Github, Plus, Menu } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const Navbar: React.FC<{ title?: string }> = ({ title }) => {
  const { user } = useAuth();
  const { toggleSidebar } = useUI();
  const navigate = useNavigate();

  return (
    <header className="h-16 px-4 sm:px-6 lg:px-8 border-b border-slate-200/90 bg-white/90 backdrop-blur-md flex items-center justify-between sticky top-0 z-10">
      <div className="flex items-center space-x-3 sm:space-x-4 min-w-0">
        <button
          onClick={toggleSidebar}
          className="p-2 -ml-1.5 rounded-xl text-slate-600 hover:text-slate-900 hover:bg-slate-100 lg:hidden transition-colors"
          aria-label="Toggle navigation menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        <h2 className="text-sm sm:text-base font-bold text-slate-900 truncate">
          {title || 'CodeReview AI'}
        </h2>
      </div>

      <div className="flex items-center space-x-2 sm:space-x-3 shrink-0">
        {!user?.githubConnected ? (
          <button
            onClick={() => navigate('/repositories')}
            className="flex items-center space-x-1.5 sm:space-x-2 px-2.5 sm:px-3.5 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold border border-slate-200 transition-colors"
          >
            <Github className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Connect GitHub</span>
            <span className="sm:hidden">Connect</span>
          </button>
        ) : (
          <div className="flex items-center space-x-1.5 px-2.5 sm:px-3 py-1.5 rounded-xl bg-emerald-50 text-emerald-600 border border-emerald-200/80 text-xs font-medium">
            <Github className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Connected</span>
          </div>
        )}

        <button
          onClick={() => navigate('/repositories')}
          className="flex items-center space-x-1.5 px-3 sm:px-4 py-1.5 sm:py-2 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition-all"
        >
          <Plus className="w-3.5 h-3.5" />
          <span className="hidden xs:inline sm:inline">New Review</span>
          <span className="xs:hidden sm:hidden">Review</span>
        </button>
      </div>
    </header>
  );
};
