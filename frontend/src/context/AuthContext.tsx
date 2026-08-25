import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, AuthResponse } from '../types/auth';
import { authService } from '../services/authService';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  login: (login: string, password: string) => Promise<void>;
  signup: (data: { name: string; username: string; email: string; password: string }) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
  updateUser: (updated: User) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        try {
          const currentUser = await authService.getCurrentUser();
          setUser(currentUser);
        } catch (err) {
          console.error('Failed to fetch current user session', err);
          localStorage.removeItem('token');
          setToken(null);
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (loginId: string, pass: string) => {
    const data: AuthResponse = await authService.login({ login: loginId, password: pass });
    localStorage.setItem('token', data.token);
    setToken(data.token);
    setUser(data.user);
  };

  const signup = async (data: { name: string; username: string; email: string; password: string }) => {
    const res: AuthResponse = await authService.signup(data);
    localStorage.setItem('token', res.token);
    setToken(res.token);
    setUser(res.user);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  const refreshUser = async () => {
    if (token) {
      try {
        const u = await authService.getCurrentUser();
        setUser(u);
      } catch (err) {
        console.error('Error refreshing user', err);
      }
    }
  };

  const updateUser = (updated: User) => {
    setUser(updated);
  };

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, signup, logout, refreshUser, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
