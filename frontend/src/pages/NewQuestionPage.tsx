import { useState } from 'react';
import { Button, Card, Form, Input, Select, Space, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/useAuth';
import { useAsk } from '@/features/questions/detailHooks';
import type { AskInput } from '@/features/questions/detailHooks';

interface AskFormValues {
  title: string;
  body: string;
  tags: string;
  courseId?: number;
}

export function NewQuestionPage() {
  const { me } = useAuth();
  const navigate = useNavigate();
  const askMutation = useAsk();
  const [form] = Form.useForm<AskFormValues>();
  const [tagText, setTagText] = useState('');

  const submit = async (values: AskFormValues) => {
    const tags = tagText
      .split(/[,，\s]+/)
      .map((t) => t.trim())
      .filter(Boolean);
    if (tags.length === 0) {
      message.warning('请至少填写 1 个标签');
      return;
    }
    if (tags.length > 5) {
      message.warning('标签最多 5 个');
      return;
    }
    const input: AskInput = {
      courseId: values.courseId,
      title: values.title.trim(),
      body: values.body.trim(),
      tags,
    };
    const { id } = await askMutation.mutateAsync(input);
    message.success('问题发布成功');
    navigate(`/questions/${id}`);
  };

  return (
    <Card title="提出一个问题" style={{ maxWidth: 800 }}>
      <Form<AskFormValues>
        form={form}
        layout="vertical"
        onFinish={submit}
        initialValues={{ courseId: undefined }}
      >
        <Form.Item
          name="title"
          label="标题（10–150 字，尽量具体）"
          rules={[
            { required: true, message: '请填写标题' },
            { min: 10, message: '标题至少 10 个字' },
            { max: 150, message: '标题最多 150 个字' },
          ]}
        >
          <Input placeholder="例如：Java 泛型擦除对重载解析有什么影响？" showCount maxLength={150} />
        </Form.Item>

        <Form.Item
          name="body"
          label="正文（Markdown，至少 20 字：说明你尝试过什么、卡在哪里）"
          rules={[
            { required: true, message: '请填写正文' },
            { min: 20, message: '正文至少 20 个字' },
            { max: 30000, message: '正文最多 30000 个字' },
          ]}
        >
          <Input.TextArea
            rows={12}
            showCount
            maxLength={30000}
            placeholder={'支持 Markdown：\n- 说明问题背景\n- 贴出你的代码（``` 包裹）\n- 描述期望与实际行为'}
          />
        </Form.Item>

        <Form.Item
          label="标签（1–5 个，用空格或逗号分隔）"
          rules={[{ required: true, message: '请填写标签' }]}
        >
          <Input value={tagText} onChange={(e) => setTagText(e.target.value)} placeholder="数据结构 红黑树" />
        </Form.Item>

        <Form.Item name="courseId" label="所属课程（可选）">
          <Select
            allowClear
            placeholder="不关联课程"
            options={(me?.courses ?? []).map((c) => ({
              value: c.id,
              label: `${c.code} ${c.name}`,
            }))}
          />
        </Form.Item>

        <Space>
          <Button type="primary" htmlType="submit" loading={askMutation.isPending}>
            发布问题
          </Button>
          <Typography.Text type="secondary">发布后页面将跳转到问题详情。</Typography.Text>
        </Space>
      </Form>
    </Card>
  );
}
