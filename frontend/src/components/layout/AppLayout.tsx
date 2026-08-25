import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Sidebar } from './Sidebar';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { Logo } from '../common/Logo';

export const AppLayout: React.FC = () => {
  const { user, token, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-slate-50 text-slate-600 space-y-4">
        <Logo size="lg" showText={false} />
        <LoadingSpinner size="md" />
        <span className="text-xs font-semibold text-slate-500 tracking-wide uppercase">Initializing CodeReview AI...</span>
      </div>
    );
  }

  if (!token || !user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="flex h-screen bg-slate-50 text-slate-800 overflow-hidden font-sans">
      <Sidebar />
      <main className="flex-1 overflow-y-auto flex flex-col min-w-0 bg-[#f8fafc]">
        <Outlet />
      </main>
    </div>
  );
};
