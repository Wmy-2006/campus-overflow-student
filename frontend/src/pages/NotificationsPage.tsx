import type { ReactNode } from 'react';
import { Badge, Button, Card, List, Typography } from 'antd';
import { Link } from 'react-router-dom';
import { useNotifications, useReadAll } from '@/features/notifications/hooks';
import { formatDate } from '@/shared/format';

export function NotificationsPage() {
  const notificationsQuery = useNotifications();
  const readAllMutation = useReadAll();

  return (
    <Card
      title="通知"
      extra={
        <Button
          size="small"
          loading={readAllMutation.isPending}
          onClick={() => readAllMutation.mutateAsync()}
        >
          全部标记为已读
        </Button>
      }
    >
      <List
        loading={notificationsQuery.isLoading}
        dataSource={notificationsQuery.data?.items ?? []}
        locale={{ emptyText: <Typography.Text type="secondary">暂时没有通知。</Typography.Text> }}
        renderItem={(n) => (
          <List.Item>
            <List.Item.Meta
              title={
                <SpaceWithBadge read={n.read}>
                  <Link to={n.link}>{n.title}</Link>
                </SpaceWithBadge>
              }
              description={<Typography.Text type="secondary">{formatDate(n.createdAt)}</Typography.Text>}
            />
          </List.Item>
        )}
      />
    </Card>
  );
}

function SpaceWithBadge({ read, children }: { read: boolean; children: ReactNode }) {
  return read ? <>{children}</> : <Badge dot>{children}</Badge>;
}
