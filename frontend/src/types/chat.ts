import { CodeReference } from './review';

export type MessageSender = 'USER' | 'AI';

export interface Message {
  id?: number;
  sender: MessageSender;
  content: string;
  references: CodeReference[];
  createdAt?: string;
}

export interface Conversation {
  id: number;
  reviewId: number;
  repositoryId: number;
  repositoryName: string;
  title: string;
  messages: Message[];
  createdAt: string;
  updatedAt: string;
}

export interface ChatResponse {
  conversationId: number;
  userMessage: Message;
  aiMessage: Message;
  references: CodeReference[];
}
