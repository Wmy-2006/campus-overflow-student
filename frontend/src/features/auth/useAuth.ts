import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '@/api/endpoints';
import type { Me } from '@/shared/types';

export const ME_KEY = ['me'] as const;

export interface LoginInput {
  username: string;
  password: string;
}

/**
 * 认证会话。
 * 「当前用户」属于服务器状态，由 TanStack Query 管理（key: ['me']）；
 * 登录/登出是变更操作，成功后直接写入缓存，避免整页刷新。
 */
export function useAuth() {
  const queryClient = useQueryClient();

  const meQuery = useQuery({
    queryKey: ME_KEY,
    queryFn: () => api.me(),
    retry: false, // 401 = 未登录，属正常状态，不重试
    staleTime: 5 * 60_000,
  });

  const loginMutation = useMutation({
    mutationFn: ({ username, password }: LoginInput) => api.login(username, password),
    onSuccess: (me: Me) => queryClient.setQueryData(ME_KEY, me),
  });

  const logoutMutation = useMutation({
    mutationFn: () => api.logout(),
    onSuccess: () => queryClient.setQueryData(ME_KEY, undefined),
  });

  return {
    me: meQuery.data ?? null,
    loading: meQuery.isLoading,
    login: loginMutation.mutateAsync,
    logout: logoutMutation.mutateAsync,
  };
}
