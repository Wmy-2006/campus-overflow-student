import type { ReactElement } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuth } from '@/features/auth/useAuth';
import { AppShell } from '@/pages/AppShell';
import { QuestionListPage } from '@/pages/QuestionListPage';
import { NewQuestionPage } from '@/pages/NewQuestionPage';
import { QuestionDetailPage } from '@/pages/QuestionDetailPage';
import { LoginPage } from '@/pages/LoginPage';
import { LeaderboardPage } from '@/pages/LeaderboardPage';
import { NotificationsPage } from '@/pages/NotificationsPage';

function RequireAuth({ children }: { children: ReactElement }) {
  const { me, loading } = useAuth();
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}>
        <Spin size="large" tip="加载中…" />
      </div>
    );
  }
  return me ? children : <Navigate to="/login" replace />;
}

export function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/" element={<Navigate to="/questions" replace />} />
        <Route path="/questions" element={<QuestionListPage />} />
        <Route
          path="/questions/new"
          element={
            <RequireAuth>
              <NewQuestionPage />
            </RequireAuth>
          }
        />
        <Route path="/questions/:id" element={<QuestionDetailPage />} />
        <Route path="/leaderboard" element={<LeaderboardPage />} />
        <Route
          path="/notifications"
          element={
            <RequireAuth>
              <NotificationsPage />
            </RequireAuth>
          }
        />
        <Route path="*" element={<Navigate to="/questions" replace />} />
      </Route>
      <Route path="/login" element={<LoginPage />} />
    </Routes>
  );
}
