import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '@/api/endpoints';
import type { QuestionDetail } from '@/shared/types';

export const QUESTION_KEY = ['question'] as const;

/** 与列表 hooks 共享的失效 key。 */
const QUESTIONS_KEY = ['questions'] as const;

export function useQuestion(questionId: number) {
  return useQuery<QuestionDetail>({
    queryKey: [...QUESTION_KEY, questionId] as const,
    queryFn: () => api.question(questionId),
  });
}

export function useRelatedQuestions(questionId: number) {
  return useQuery({
    queryKey: [...QUESTION_KEY, questionId, 'related'] as const,
    queryFn: () => api.related(questionId),
    enabled: Number.isFinite(questionId) && questionId > 0,
  });
}

export interface AskInput {
  courseId?: number;
  title: string;
  body: string;
  tags: string[];
}

/** 发布问题：成功后失效列表与热门标签，返回新问题 id 供跳转详情。 */
export function useAsk() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: AskInput) => api.ask(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUESTIONS_KEY });
    },
  });
}

/** 详情页内变更：投票 / 回答 / 评论 / 采纳 / 认证 / 悬赏，成功后失效详情缓存。 */
export function useQuestionMutations(questionId: number) {
  const queryClient = useQueryClient();
  const invalidateDetail = () => queryClient.invalidateQueries({ queryKey: [...QUESTION_KEY, questionId] });

  const vote = useMutation({
    mutationFn: (value: number) => api.vote('QUESTION', questionId, value),
    onSuccess: invalidateDetail,
  });
  const answer = useMutation({
    mutationFn: (body: string) => api.answer(questionId, body),
    onSuccess: invalidateDetail,
  });
  const comment = useMutation({
    mutationFn: (body: string) => api.comment('QUESTION', questionId, body),
    onSuccess: invalidateDetail,
  });
  const accept = useMutation({
    mutationFn: (answerId: number) => api.accept(questionId, answerId),
    onSuccess: invalidateDetail,
  });
  const endorse = useMutation({
    mutationFn: (answerId: number) => api.endorse(answerId),
    onSuccess: invalidateDetail,
  });
  const bounty = useMutation({
    mutationFn: ({ points, days }: { points: number; days: number }) => api.bounty(questionId, points, days),
    onSuccess: invalidateDetail,
  });
  const answerVote = useMutation({
    mutationFn: ({ answerId, value }: { answerId: number; value: number }) =>
      api.vote('ANSWER', answerId, value),
    onSuccess: invalidateDetail,
  });
  const answerComment = useMutation({
    mutationFn: ({ answerId, body }: { answerId: number; body: string }) =>
      api.comment('ANSWER', answerId, body),
    onSuccess: invalidateDetail,
  });
  const remove = useMutation({
    mutationFn: () => api.deleteQuestion(questionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUESTIONS_KEY });
    },
  });

  return { vote, answer, comment, accept, endorse, bounty, answerVote, answerComment, remove };
}
