import api from './api';
import { ChatResponse, Conversation } from '../types/chat';

export const chatService = {
  async sendMessage(reviewId: number, data: { message: string; conversationId?: number }): Promise<ChatResponse> {
    const res = await api.post<ChatResponse>(`/reviews/${reviewId}/chat`, data);
    return res.data;
  },

  async getConversation(id: number): Promise<Conversation> {
    const res = await api.get<Conversation>(`/conversations/${id}`);
    return res.data;
  },

  async getConversationsByReview(reviewId: number): Promise<Conversation[]> {
    const res = await api.get<Conversation[]>(`/reviews/${reviewId}/conversations`);
    return res.data;
  }
};
