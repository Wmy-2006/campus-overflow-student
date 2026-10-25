import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { BrowserRouter } from 'react-router-dom';
import { queryClient } from '@/shared/queryClient';
import { api } from '@/api/endpoints';
import { App } from './App';
import './styles.css';

// 启动即拉一次 CSRF，让 XSRF-TOKEN Cookie 就位（后续非 GET 请求由拦截器兜底）
api.csrf().catch(() => undefined);

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider locale={zhCN}>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </QueryClientProvider>
    </ConfigProvider>
  </React.StrictMode>,
);
