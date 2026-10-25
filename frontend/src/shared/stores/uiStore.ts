import { create } from 'zustand';

/**
 * 纯客户端状态（与服务器数据无关）：
 * - pendingRequests：由 axios 拦截器维护的进行中请求计数，>0 时展示全局加载条
 * - sidebarCollapsed：侧边栏折叠状态
 */
interface UiState {
  pendingRequests: number;
  sidebarCollapsed: boolean;
  beginRequest: () => void;
  endRequest: () => void;
  toggleSidebar: () => void;
}

export const useUiStore = create<UiState>((set) => ({
  pendingRequests: 0,
  sidebarCollapsed: false,
  beginRequest: () => set((s) => ({ pendingRequests: s.pendingRequests + 1 })),
  endRequest: () => set((s) => ({ pendingRequests: Math.max(0, s.pendingRequests - 1) })),
  toggleSidebar: () => set((s) => ({ sidebarCollapsed: !s.sidebarCollapsed })),
}));
