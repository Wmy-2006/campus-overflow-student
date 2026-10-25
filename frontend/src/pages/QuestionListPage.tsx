import { useMemo, useState } from 'react';
import { Alert, Button, Checkbox, Input, List, Pagination, Select, Space, Tag, Typography } from 'antd';
import { Link } from 'react-router-dom';
import { useAuth } from '@/features/auth/useAuth';
import { usePopularTags, useQuestions } from '@/features/questions/listHooks';
import { formatDate } from '@/shared/format';

const PAGE_SIZE = 20;

const SORT_OPTIONS = [
  { value: 'newest', label: '最新' },
  { value: 'active', label: '最近活跃' },
  { value: 'hot', label: '得分最高' },
  { value: 'relevance', label: '相关度' },
];

export function QuestionListPage() {
  const { me } = useAuth();
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [activeTag, setActiveTag] = useState<string | undefined>();
  const [sort, setSort] = useState('newest');
  const [unanswered, setUnanswered] = useState(false);
  const [page, setPage] = useState(1);

  const filters = useMemo(
    () => ({ page, keyword: searchInput, tag: activeTag, sort, unanswered }),
    [page, searchInput, activeTag, sort, unanswered],
  );
  const questionsQuery = useQuestions(filters);
  const tagsQuery = usePopularTags();
  const data = questionsQuery.data;

  const search = () => {
    setPage(1);
    setSearchInput(keyword.trim());
  };

  return (
    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
      <Space wrap>
        <Input.Search
          placeholder="搜索问题（支持中文全文检索）"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onSearch={search}
          allowClear
          style={{ width: 320 }}
        />
        <Select
          value={sort}
          onChange={(v) => {
            setSort(v);
            setPage(1);
          }}
          options={SORT_OPTIONS}
          style={{ width: 140 }}
        />
        <Checkbox
          checked={unanswered}
          onChange={(e) => {
            setUnanswered(e.target.checked);
            setPage(1);
          }}
        >
          仅看未回答
        </Checkbox>
        {me && (
          <Link to="/questions/new">
            <Button type="primary">我要提问</Button>
          </Link>
        )}
      </Space>

      {activeTag && (
        <Space>
          <Typography.Text type="secondary">标签筛选：</Typography.Text>
          <Tag color="blue">{activeTag}</Tag>
          <Button size="small" onClick={() => setActiveTag(undefined)}>
            清除
          </Button>
        </Space>
      )}

      {questionsQuery.isError && (
        <Alert type="error" showIcon message={questionsQuery.error?.message ?? '加载失败'} />
      )}

      <List
        loading={questionsQuery.isLoading}
        dataSource={data?.items ?? []}
        locale={{ emptyText: '没有匹配的问题，换个关键词试试。' }}
        renderItem={(q) => (
          <List.Item
            style={{ alignItems: 'flex-start', gap: 16 }}
            actions={[
              <Tag key="score">{q.score} 票</Tag>,
              <Tag key="answers" color={q.accepted ? 'green' : 'default'}>
                {q.answerCount} 回答
              </Tag>,
              <Tag key="views">{q.viewCount} 浏览</Tag>,
            ]}
          >
            <List.Item.Meta
              title={
                <Link to={`/questions/${q.id}`} style={{ color: 'inherit' }}>
                  <Typography.Text strong style={{ fontSize: 16 }}>
                    {q.title}
                  </Typography.Text>
                </Link>
              }
              description={
                <Space direction="vertical" size={4} style={{ width: '100%' }}>
                  <Typography.Paragraph type="secondary" ellipsis={{ rows: 2 }} style={{ marginBottom: 0 }}>
                    {q.excerpt}
                  </Typography.Paragraph>
                  <Space wrap size={[4, 4]}>
                    {q.tags.map((t) => (
                      <Tag key={t} style={{ cursor: 'pointer' }} onClick={() => setActiveTag(t)}>
                        {t}
                      </Tag>
                    ))}
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                      {q.author.displayName} · {formatDate(q.createdAt)}
                    </Typography.Text>
                  </Space>
                </Space>
              }
            />
          </List.Item>
        )}
      />

      {data && data.total > PAGE_SIZE && (
        <Pagination
          current={data.page}
          pageSize={PAGE_SIZE}
          total={data.total}
          onChange={setPage}
          showSizeChanger={false}
        />
      )}

      {!me && (
        <Typography.Text type="secondary">
          <Link to="/login">登录</Link> 后可以提问、回答与投票。
        </Typography.Text>
      )}

      <div>
        <Typography.Title level={5}>热门标签</Typography.Title>
        <Space wrap>
          {(tagsQuery.data ?? []).map((t) => (
            <Tag key={t.name} style={{ cursor: 'pointer' }} onClick={() => setActiveTag(t.name)}>
              {t.name} <span style={{ opacity: 0.6 }}>{t.questionCount}</span>
            </Tag>
          ))}
        </Space>
      </div>
    </Space>
  );
}
