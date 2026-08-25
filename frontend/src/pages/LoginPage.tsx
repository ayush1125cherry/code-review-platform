import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Logo } from '../components/common/Logo';
import { ArrowRight } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!loginId || !password) return;

    setError(null);
    setLoading(true);
    try {
      await login(loginId, password);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#f8fafc] flex flex-col justify-center items-center px-4 sm:px-6 lg:px-8 relative">
      <div className="w-full max-w-md space-y-8 relative z-10">
        {/* Brand */}
        <div className="flex flex-col items-center text-center space-y-2">
          <Logo size="lg" showText={false} className="mb-2" />
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">CodeReview AI</h2>
          <p className="text-xs sm:text-sm text-slate-500">Sign in to your AI Code Review workspace</p>
        </div>

        {/* Card */}
        <div className="bg-white border border-slate-200/90 rounded-2xl p-6 sm:p-8 shadow-xl space-y-6">
          {error && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-medium">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1.5">
                Username or Email
              </label>
              <input
                type="text"
                required
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                placeholder="e.g. dev_alex or alex@example.com"
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 placeholder-slate-400 text-sm focus:bg-white focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 transition-all"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1.5">
                Password
              </label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 placeholder-slate-400 text-sm focus:bg-white focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 transition-all"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 flex items-center justify-center space-x-2 py-2.5 px-4 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white font-semibold text-sm shadow-xs transition-all disabled:opacity-50"
            >
              <span>{loading ? 'Signing in...' : 'Sign In'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="text-center pt-2 border-t border-slate-100">
            <p className="text-xs text-slate-500">
              Don't have an account?{' '}
              <Link to="/signup" className="text-indigo-600 hover:text-indigo-800 font-semibold underline underline-offset-2">
                Create one now
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
