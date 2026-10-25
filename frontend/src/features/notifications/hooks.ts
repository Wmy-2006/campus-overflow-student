import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '@/api/endpoints';

export const NOTIFICATIONS_KEY = ['notifications'] as const;

export function useNotifications() {
  return useQuery({
    queryKey: NOTIFICATIONS_KEY,
    queryFn: () => api.notifications(),
  });
}

export function useUnreadCount(enabled: boolean) {
  return useQuery({
    queryKey: [...NOTIFICATIONS_KEY, 'unread'] as const,
    queryFn: () => api.unreadCount(),
    enabled,
    refetchInterval: 30_000,
  });
}

export function useReadAll() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.readAll(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NOTIFICATIONS_KEY });
    },
  });
}
