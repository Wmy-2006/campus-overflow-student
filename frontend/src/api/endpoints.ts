import { request } from './client';
import type {
  LeaderboardEntry,
  Me,
  Notification,
  Page,
  PopularTag,
  QuestionDetail,
  QuestionSummary,
  ReputationProfile,
  SearchResult,
} from '@/shared/types';

/** 全部后端接口的类型化封装（与后端 QuestionController / AuthController 等一一对应）。 */
export const api = {
  csrf: () => request<{ headerName: string; token: string }>({ method: 'GET', url: '/v1/auth/csrf' }),

  register: (body: {
    username: string;
    displayName: string;
    email: string;
    password: string;
    college?: string;
    className?: string;
  }) => request<{ id: number }>({ method: 'POST', url: '/v1/auth/register', data: body }),

  login: (username: string, password: string) =>
    request<Me>({ method: 'POST', url: '/v1/auth/login', data: { username, password } }),

  logout: () => request<void>({ method: 'POST', url: '/v1/auth/logout' }),

  me: () => request<Me>({ method: 'GET', url: '/v1/auth/me' }),

  questions: (page = 1, courseId?: number) =>
    request<Page<QuestionSummary>>({
      method: 'GET',
      url: '/v1/questions',
      params: { page, ...(courseId ? { courseId } : {}) },
    }),

  question: (id: number) => request<QuestionDetail>({ method: 'GET', url: `/v1/questions/${id}` }),

  ask: (body: { courseId?: number; title: string; body: string; tags: string[] }) =>
    request<{ id: number }>({ method: 'POST', url: '/v1/questions', data: body }),

  deleteQuestion: (id: number) => request<void>({ method: 'DELETE', url: `/v1/questions/${id}` }),

  answer: (questionId: number, body: string) =>
    request<{ id: number }>({ method: 'POST', url: `/v1/questions/${questionId}/answers`, data: { body } }),

  accept: (questionId: number, answerId: number) =>
    request<void>({ method: 'PUT', url: `/v1/questions/${questionId}/accepted-answer`, data: { answerId } }),

  endorse: (answerId: number) =>
    request<void>({ method: 'POST', url: `/v1/answers/${answerId}/endorsement`, data: {} }),

  vote: (targetType: 'QUESTION' | 'ANSWER', targetId: number, value: number) =>
    request<{ myVote: number; scoreDelta: number }>({
      method: 'PUT',
      url: '/v1/votes',
      data: { targetType, targetId, value },
    }),

  comment: (targetType: 'QUESTION' | 'ANSWER', targetId: number, body: string, parentId?: number) =>
    request<{ id: number }>({
      method: 'POST',
      url: '/v1/comments',
      data: { targetType, targetId, body, ...(parentId ? { parentId } : {}) },
    }),

  search: (q: string, params: { tag?: string; sort?: string; unanswered?: boolean; page?: number } = {}) => {
    const search = new URLSearchParams({ q, page: String(params.page ?? 1) });
    if (params.tag) search.set('tag', params.tag);
    if (params.sort) search.set('sort', params.sort);
    if (params.unanswered) search.set('unanswered', 'true');
    return request<Page<SearchResult>>({ method: 'GET', url: `/v1/search/questions?${search}` });
  },

  related: (questionId: number) =>
    request<{ id: number; title: string; answerCount: number; accepted: boolean }[]>({
      method: 'GET',
      url: `/v1/questions/${questionId}/related`,
    }),

  popularTags: () => request<PopularTag[]>({ method: 'GET', url: '/v1/tags/popular?limit=15' }),

  leaderboard: (period: string) =>
    request<LeaderboardEntry[]>({ method: 'GET', url: '/v1/leaderboard', params: { period } }),

  reputation: (userId: number) =>
    request<ReputationProfile>({ method: 'GET', url: `/v1/users/${userId}/reputation` }),

  bounty: (questionId: number, points: number, days: number) =>
    request<{ id: number }>({
      method: 'POST',
      url: `/v1/questions/${questionId}/bounty`,
      data: { points, days },
    }),

  notifications: () => request<Page<Notification>>({ method: 'GET', url: '/v1/notifications' }),

  unreadCount: () => request<{ count: number }>({ method: 'GET', url: '/v1/notifications/unread-count' }),

  readAll: () =>
    request<{ updated: number }>({ method: 'POST', url: '/v1/notifications/read-all', data: {} }),
};
