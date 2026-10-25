import { QueryClient } from '@tanstack/react-query';
import { message } from 'antd';
import { ApiError } from '@/api/errors';

/**
 * TanStack Query 全局配置。
 * 服务器状态（问题列表/详情/标签/排行榜/通知/当前用户）统一由 Query 管理；
 * 变更操作走 Mutation，成功后失效(invalidate)对应 key 触发重新拉取。
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 15_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
      onError: (err) => {
        const text = err instanceof ApiError ? err.message : '操作失败，请稍后重试';
        message.error(text);
      },
    },
  },
});
