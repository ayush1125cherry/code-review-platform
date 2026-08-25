import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useUI } from '../../context/UIContext';
import { Logo } from '../common/Logo';
import {
  LayoutDashboard,
  GitBranch,
  FileCheck2,
  User as UserIcon,
  LogOut,
  Github,
  X
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const { user, logout } = useAuth();
  const { isSidebarOpen, setIsSidebarOpen } = useUI();
  const navigate = useNavigate();

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { label: 'Repositories', path: '/repositories', icon: GitBranch },
    { label: 'Reviews', path: '/reviews', icon: FileCheck2 },
    { label: 'Profile', path: '/profile', icon: UserIcon },
  ];

  const handleLogout = () => {
    setIsSidebarOpen(false);
    logout();
    navigate('/login');
  };

  const navContent = (
    <div className="flex flex-col justify-between h-full">
      {/* Brand Header */}
      <div>
        <div className="p-5 sm:p-6 border-b border-slate-100 flex items-center justify-between">
          <Logo size="md" />
          <button
            onClick={() => setIsSidebarOpen(false)}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 lg:hidden"
            aria-label="Close menu"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-1">
          {navItems.map(({ label, path, icon: Icon }) => (
            <NavLink
              key={path}
              to={path}
              onClick={() => setIsSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3.5 py-2.5 rounded-xl text-xs transition-all ${
                  isActive
                    ? 'bg-indigo-50/90 text-indigo-600 font-bold border border-indigo-100/80 shadow-xs'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50 font-medium border border-transparent'
                }`
              }
            >
              <Icon className="w-4 h-4" />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      {/* User info & Logout */}
      <div className="p-4 border-t border-slate-100 space-y-3">
        {user && (
          <div className="flex items-center space-x-3 px-2">
            <img
              src={user.avatarUrl || `https://api.dicebear.com/7.x/identicon/svg?seed=${user.username}`}
              alt={user.name}
              className="w-8 h-8 rounded-full border border-slate-200 object-cover shrink-0"
            />
            <div className="min-w-0 flex-1">
              <p className="text-xs font-bold text-slate-800 truncate">{user.name}</p>
              <div className="flex items-center space-x-1 text-[11px] text-slate-500 truncate">
                {user.githubConnected ? (
                  <span className="text-slate-500 flex items-center space-x-1 truncate">
                    <Github className="w-3 h-3 inline text-slate-400 shrink-0" />
                    <span>@{user.githubUsername || user.username}</span>
                  </span>
                ) : (
                  <span>@{user.username}</span>
                )}
              </div>
            </div>
          </div>
        )}

        <button
          onClick={handleLogout}
          className="w-full flex items-center space-x-2.5 px-3.5 py-2 rounded-xl text-xs font-medium text-slate-600 hover:bg-rose-50 hover:text-rose-600 transition-colors border border-transparent"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop Persistent Sidebar */}
      <aside className="hidden lg:flex w-64 bg-white border-r border-slate-200/90 flex-col justify-between shrink-0 h-screen sticky top-0 z-20 shadow-xs">
        {navContent}
      </aside>

      {/* Mobile Drawer Overlay */}
      {isSidebarOpen && (
        <div
          onClick={() => setIsSidebarOpen(false)}
          className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs z-40 lg:hidden transition-opacity duration-300 animate-in fade-in"
        />
      )}

      {/* Mobile Drawer Sidebar */}
      <aside
        className={`fixed top-0 left-0 bottom-0 w-72 max-w-[80vw] bg-white z-50 flex flex-col justify-between shadow-2xl transition-transform duration-300 ease-in-out lg:hidden ${
          isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {navContent}
      </aside>
    </>
  );
};
