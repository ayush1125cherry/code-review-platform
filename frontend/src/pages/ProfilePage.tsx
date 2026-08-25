import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';
import { githubService } from '../services/githubService';
import { Navbar } from '../components/layout/Navbar';
import {
  User as UserIcon,
  Github,
  Key,
  Lock,
  CheckCircle2,
  AlertCircle,
  Save,
  LogOut
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const ProfilePage: React.FC = () => {
  const { user, refreshUser, logout } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState(user?.name || '');
  const [email, setEmail] = useState(user?.email || '');
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const [profileMsg, setProfileMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [passwordMsg, setPasswordMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [savingProfile, setSavingProfile] = useState(false);
  const [changingPass, setChangingPass] = useState(false);

  useEffect(() => {
    if (user) {
      setName(user.name || '');
      setEmail(user.email || '');
      setAvatarUrl(user.avatarUrl || '');
    }
  }, [user]);

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setSavingProfile(true);
    setProfileMsg(null);
    try {
      await authService.updateProfile({
        name,
        email,
        avatarUrl: avatarUrl || undefined,
      });
      await refreshUser();
      setProfileMsg({ type: 'success', text: 'Profile updated successfully!' });
    } catch (err: any) {
      setProfileMsg({ type: 'error', text: err.response?.data?.message || 'Failed to update profile' });
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentPassword || !newPassword) return;

    setChangingPass(true);
    setPasswordMsg(null);
    try {
      await authService.changePassword({ currentPassword, newPassword });
      setCurrentPassword('');
      setNewPassword('');
      setPasswordMsg({ type: 'success', text: 'Password updated successfully!' });
    } catch (err: any) {
      setPasswordMsg({ type: 'error', text: err.response?.data?.message || 'Failed to change password' });
    } finally {
      setChangingPass(false);
    }
  };

  const handleDisconnectGithub = async () => {
    if (confirm('Are you sure you want to disconnect your GitHub account?')) {
      try {
        await githubService.disconnect();
        await refreshUser();
      } catch (err) {
        console.error('Error disconnecting GitHub', err);
      }
    }
  };

  return (
    <div className="flex-1 flex flex-col min-w-0 bg-[#f8fafc]">
      <Navbar title="User Profile & Settings" />

      <div className="p-4 sm:p-6 lg:p-8 max-w-4xl w-full mx-auto space-y-6 sm:space-y-8">
        {/* Profile Card Header */}
        <div className="bg-white border border-slate-200/90 rounded-2xl p-5 sm:p-8 shadow-xs flex flex-col sm:flex-row items-center space-y-4 sm:space-y-0 sm:space-x-6">
          <img
            src={user?.avatarUrl || `https://api.dicebear.com/7.x/identicon/svg?seed=${user?.username}`}
            alt={user?.name}
            className="w-16 h-16 sm:w-20 sm:h-20 rounded-2xl border-2 border-indigo-100 object-cover shadow-xs"
          />

          <div className="text-center sm:text-left space-y-1 min-w-0 flex-1">
            <h2 className="text-lg sm:text-xl font-bold text-slate-900 truncate">{user?.name}</h2>
            <p className="text-xs text-slate-500 font-mono">@{user?.username} • {user?.email}</p>
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2 pt-2">
              {user?.githubConnected ? (
                <span className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 text-xs font-medium">
                  <Github className="w-3.5 h-3.5" />
                  <span>GitHub Connected (@{user.githubUsername})</span>
                </span>
              ) : (
                <span className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full bg-slate-100 text-slate-600 border border-slate-200 text-xs font-medium">
                  <Github className="w-3.5 h-3.5" />
                  <span>GitHub Not Connected</span>
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Edit Profile Form */}
        <div className="bg-white border border-slate-200/90 rounded-2xl p-5 sm:p-6 shadow-xs space-y-6">
          <div className="flex items-center space-x-3 pb-4 border-b border-slate-100">
            <div className="p-2 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100 shrink-0">
              <UserIcon className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 text-base">Edit Profile Information</h3>
              <p className="text-xs text-slate-500">Update your name and avatar preferences</p>
            </div>
          </div>

          {profileMsg && (
            <div className={`p-3 rounded-xl text-xs font-medium flex items-center space-x-2 ${profileMsg.type === 'success' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-rose-50 text-rose-700 border border-rose-200'}`}>
              {profileMsg.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
              <span>{profileMsg.text}</span>
            </div>
          )}

          <form onSubmit={handleUpdateProfile} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1.5">
                  Full Name
                </label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-xs focus:bg-white focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20"
                />
              </div>

              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Email Address
                  </label>
                  <span className="flex items-center space-x-1 text-[10px] font-medium text-slate-400">
                    <Lock className="w-2.5 h-2.5" />
                    <span>Permanent</span>
                  </span>
                </div>
                <input
                  type="email"
                  disabled
                  readOnly
                  value={user?.email || email}
                  className="w-full px-3.5 py-2.5 bg-slate-100/80 border border-slate-200 rounded-xl text-slate-400 text-xs cursor-not-allowed select-none focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1.5">
                Avatar Image URL (optional)
              </label>
              <input
                type="url"
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
                placeholder="https://..."
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-xs focus:bg-white focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20"
              />
            </div>

            <div className="pt-2 flex justify-end">
              <button
                type="submit"
                disabled={savingProfile}
                className="flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-[#4f46e5] hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition-all disabled:opacity-50"
              >
                <Save className="w-3.5 h-3.5" />
                <span>{savingProfile ? 'Saving...' : 'Save Profile'}</span>
              </button>
            </div>
          </form>
        </div>

        {/* Change Password Form */}
        <div className="bg-white border border-slate-200/90 rounded-2xl p-5 sm:p-6 shadow-xs space-y-6">
          <div className="flex items-center space-x-3 pb-4 border-b border-slate-100">
            <div className="p-2 rounded-xl bg-purple-50 text-purple-600 border border-purple-100 shrink-0">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 text-base">Change Password</h3>
              <p className="text-xs text-slate-500">Ensure your account is using a secure password</p>
            </div>
          </div>

          {passwordMsg && (
            <div className={`p-3 rounded-xl text-xs font-medium flex items-center space-x-2 ${passwordMsg.type === 'success' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-rose-50 text-rose-700 border border-rose-200'}`}>
              {passwordMsg.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
              <span>{passwordMsg.text}</span>
            </div>
          )}

          <form onSubmit={handleChangePassword} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1.5">
                  Current Password
                </label>
                <input
                  type="password"
                  required
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-xs focus:bg-white focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1.5">
                  New Password
                </label>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Minimum 6 characters"
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 text-xs focus:bg-white focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20"
                />
              </div>
            </div>

            <div className="pt-2 flex justify-end">
              <button
                type="submit"
                disabled={changingPass}
                className="flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-900 text-white text-xs font-semibold transition-colors disabled:opacity-50"
              >
                <Key className="w-3.5 h-3.5" />
                <span>{changingPass ? 'Updating...' : 'Update Password'}</span>
              </button>
            </div>
          </form>
        </div>

        {/* GitHub & Account Actions */}
        <div className="bg-white border border-slate-200/90 rounded-2xl p-5 sm:p-6 shadow-xs space-y-4">
          <h3 className="font-bold text-slate-900 text-base">Account Actions</h3>
          <div className="flex flex-wrap gap-3">
            {user?.githubConnected && (
              <button
                onClick={handleDisconnectGithub}
                className="px-4 py-2 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-700 text-xs font-semibold border border-rose-200 transition-colors"
              >
                Disconnect GitHub Account
              </button>
            )}

            <button
              onClick={() => {
                logout();
                navigate('/login');
              }}
              className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold border border-slate-200 transition-colors"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Sign Out</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
