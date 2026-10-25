import { Badge, Button, Dropdown, Layout, Menu, Space, Typography } from 'antd';
import type { MenuProps } from 'antd';
import {
  BellOutlined,
  LogoutOutlined,
  PlusOutlined,
  QuestionCircleOutlined,
  TrophyOutlined,
} from '@ant-design/icons';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/useAuth';
import { useUnreadCount } from '@/features/notifications/hooks';
import { useUiStore } from '@/shared/stores/uiStore';
import { GlobalLoading } from '@/shared/components/GlobalLoading';

const { Header, Sider, Content } = Layout;

const MENU_ITEMS: MenuProps['items'] = [
  { key: '/questions', icon: <QuestionCircleOutlined />, label: <Link to="/questions">问题列表</Link> },
  { key: '/questions/new', icon: <PlusOutlined />, label: <Link to="/questions/new">我要提问</Link> },
  { key: '/leaderboard', icon: <TrophyOutlined />, label: <Link to="/leaderboard">排行榜</Link> },
  { key: '/notifications', icon: <BellOutlined />, label: <Link to="/notifications">通知</Link> },
];

export function AppShell() {
  const location = useLocation();
  const navigate = useNavigate();
  const { me, logout } = useAuth();
  const collapsed = useUiStore((s) => s.sidebarCollapsed);
  const toggleSidebar = useUiStore((s) => s.toggleSidebar);
  const unread = useUnreadCount(!!me);

  const userMenu: MenuProps['items'] = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        logout().then(() => navigate('/questions'));
      },
    },
  ];

  const selectedKey = ['/questions/new', '/leaderboard', '/notifications'].includes(location.pathname)
    ? location.pathname
    : '/questions';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <GlobalLoading />
      <Header
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 16,
          background: '#fff',
          borderBottom: '1px solid #f0f0f0',
          paddingInline: 24,
        }}
      >
        <Link to="/questions">
          <Typography.Title level={4} style={{ margin: 0, color: '#1677ff' }}>
            CampusOverflow
          </Typography.Title>
        </Link>
        <div style={{ flex: 1 }} />
        {me ? (
          <Space size="middle">
            <Badge count={unread.data?.count ?? 0} size="small">
              <Button type="text" icon={<BellOutlined />} onClick={() => navigate('/notifications')} />
            </Badge>
            <Dropdown menu={{ items: userMenu }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                <span style={{ color: 'rgba(0,0,0,0.65)' }}>{me.displayName}</span>
                <span
                  style={{
                    fontSize: 12,
                    padding: '0 6px',
                    borderRadius: 4,
                    background: '#e6f4ff',
                    color: '#0958d9',
                  }}
                >
                  {me.role}
                </span>
              </Space>
            </Dropdown>
          </Space>
        ) : (
          <Button type="primary" onClick={() => navigate('/login')}>
            登录 / 注册
          </Button>
        )}
      </Header>
      <Layout>
        <Sider
          theme="light"
          collapsible
          collapsed={collapsed}
          onCollapse={toggleSidebar}
          width={200}
          style={{ borderRight: '1px solid #f0f0f0' }}
        >
          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            items={MENU_ITEMS}
            style={{ borderInlineEnd: 'none' }}
          />
        </Sider>
        <Content style={{ padding: 24 }}>
          <div style={{ maxWidth: 1080, margin: '0 auto' }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
