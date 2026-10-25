import { useQuery } from '@tanstack/react-query';
import { api } from '@/api/endpoints';
import type { PopularTag } from '@/shared/types';

export const QUESTIONS_KEY = ['questions'] as const;

export interface QuestionFilters {
  page: number;
  keyword: string;
  tag?: string;
  sort: string;
  unanswered: boolean;
}

export function questionsQueryKey(filters: QuestionFilters) {
  return [...QUESTIONS_KEY, filters] as const;
}

/** 列表：搜索/标签/排序/只看未回答任一条件生效时走全文检索，否则走普通分页列表。 */
export function useQuestions(filters: QuestionFilters) {
  return useQuery({
    queryKey: questionsQueryKey(filters),
    queryFn: () => {
      const searching =
        filters.keyword.trim() !== '' || !!filters.tag || filters.unanswered || filters.sort !== 'newest';
      if (searching) {
        return api.search(filters.keyword.trim(), {
          tag: filters.tag,
          sort: filters.sort,
          unanswered: filters.unanswered,
          page: filters.page,
        });
      }
      return api.questions(filters.page);
    },
  });
}

export function usePopularTags() {
  return useQuery<PopularTag[]>({
    queryKey: ['tags', 'popular'] as const,
    queryFn: () => api.popularTags(),
  });
}
