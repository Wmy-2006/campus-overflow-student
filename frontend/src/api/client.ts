import axios, { type AxiosError, type AxiosRequestConfig } from 'axios';
import { toApiError } from './errors';
import { useUiStore } from '@/shared/stores/uiStore';

function readCookie(name: string): string | undefined {
  return document.cookie
    .split('; ')
    .find((c) => c.startsWith(`${name}=`))
    ?.split('=')[1];
}

/** 确保 XSRF-TOKEN Cookie 存在（CSRF 双提交的前置），不存在时先拉一次 csrf 端点。 */
async function ensureCsrfToken(): Promise<string | undefined> {
  let token = readCookie('XSRF-TOKEN');
  if (!token) {
    await fetch('/api/v1/auth/csrf', { credentials: 'include' });
    token = readCookie('XSRF-TOKEN');
  }
  return token ? decodeURIComponent(token) : undefined;
}

export const http = axios.create({
  baseURL: '/api',
  withCredentials: true, // 会话 Cookie（Spring Session + Redis）与 XSRF-TOKEN Cookie 随请求携带
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截：非 GET 请求注入 X-XSRF-TOKEN（值取自同源 Cookie，做 URL 解码）
http.interceptors.request.use(async (config) => {
  const method = config.method?.toLowerCase();
  if (method && method !== 'get') {
    const token = await ensureCsrfToken();
    if (token) {
      config.headers.set('X-XSRF-TOKEN', token);
    }
  }
  useUiStore.getState().beginRequest();
  return config;
});

// 响应拦截：错误统一转为 ApiError（RFC 9457 Problem Details），并维护全局加载计数
http.interceptors.response.use(
  (response) => {
    useUiStore.getState().endRequest();
    return response;
  },
  (error: AxiosError) => {
    useUiStore.getState().endRequest();
    if (error.response) {
      throw toApiError(error.response.status, error.response.data, '请求失败');
    }
    throw toApiError(0, null, error.message || '网络异常，请检查后端服务是否启动');
  },
);

// 类型安全的请求封装：返回数据本体（204 返回 undefined）
export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<T>(config);
  return response.data as T;
}
