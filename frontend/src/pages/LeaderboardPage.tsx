import { useState } from 'react';
import { Alert, Card, Radio, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/api/endpoints';
import type { LeaderboardEntry } from '@/shared/types';

const PERIODS = [
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' },
  { value: 'term', label: '本学期' },
  { value: 'all', label: '总榜' },
];

const columns: ColumnsType<LeaderboardEntry> = [
  { title: '名次', dataIndex: 'rank', width: 80 },
  { title: '用户', dataIndex: 'displayName' },
  { title: '声誉', dataIndex: 'score', width: 120 },
];

export function LeaderboardPage() {
  const [period, setPeriod] = useState('week');

  const leaderboardQuery = useQuery({
    queryKey: ['leaderboard', period] as const,
    queryFn: () => api.leaderboard(period),
  });

  return (
    <Card
      title="声誉排行榜"
      extra={
        <Radio.Group
          optionType="button"
          value={period}
          onChange={(e) => setPeriod(e.target.value)}
          options={PERIODS}
        />
      }
    >
      {leaderboardQuery.isError && (
        <Alert type="error" showIcon message={leaderboardQuery.error?.message ?? '加载失败'} />
      )}
      <Table<LeaderboardEntry>
        rowKey="userId"
        loading={leaderboardQuery.isLoading}
        columns={columns}
        dataSource={leaderboardQuery.data ?? []}
        pagination={false}
        locale={{ emptyText: <Typography.Text type="secondary">这个周期还没有数据。</Typography.Text> }}
      />
    </Card>
  );
}
