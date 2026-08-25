import api from './api';
import { ReviewResponse } from '../types/review';

export const reviewService = {
  async getReview(id: number): Promise<ReviewResponse> {
    const res = await api.get<ReviewResponse>(`/reviews/${id}`);
    return res.data;
  },

  async getReviewsByRepository(repoId: number): Promise<ReviewResponse[]> {
    const res = await api.get<ReviewResponse[]>(`/reviews/repository/${repoId}`);
    return res.data;
  },

  async getRecentReviews(): Promise<ReviewResponse[]> {
    const res = await api.get<ReviewResponse[]>('/reviews/recent');
    return res.data;
  },

  async deleteReview(id: number): Promise<void> {
    await api.delete(`/reviews/${id}`);
  }
};
