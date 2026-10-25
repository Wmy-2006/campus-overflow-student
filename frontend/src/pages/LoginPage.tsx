import { Button, Card, Form, Input, Tabs, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/useAuth';
import { api } from '@/api/endpoints';

interface LoginValues {
  username: string;
  password: string;
}

interface RegisterValues extends LoginValues {
  displayName: string;
  email: string;
  college?: string;
  className?: string;
}

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [loginForm] = Form.useForm<LoginValues>();
  const [registerForm] = Form.useForm<RegisterValues>();

  const doLogin = async (values: LoginValues) => {
    await login(values);
    message.success('登录成功');
    navigate('/questions');
  };

  const doRegister = async (values: RegisterValues) => {
    await api.register(values);
    await login({ username: values.username, password: values.password });
    message.success('注册成功，已自动登录');
    navigate('/questions');
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: '#f5f5f5',
      }}
    >
      <Card style={{ width: 420 }}>
        <Tabs
          defaultActiveKey="login"
          centered
          items={[
            {
              key: 'login',
              label: '登录',
              children: (
                <Form<LoginValues> form={loginForm} layout="vertical" onFinish={doLogin}>
                  <Form.Item
                    name="username"
                    label="学号 / 工号"
                    rules={[{ required: true, message: '请输入学号 / 工号' }]}
                  >
                    <Input />
                  </Form.Item>
                  <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
                    <Input.Password />
                  </Form.Item>
                  <Button type="primary" htmlType="submit" block loading={false}>
                    登录
                  </Button>
                </Form>
              ),
            },
            {
              key: 'register',
              label: '注册',
              children: (
                <Form<RegisterValues> form={registerForm} layout="vertical" onFinish={doRegister}>
                  <Form.Item
                    name="username"
                    label="学号 / 工号（4–20 位）"
                    rules={[{ required: true, message: '请输入学号 / 工号' }]}
                  >
                    <Input />
                  </Form.Item>
                  <Form.Item
                    name="password"
                    label="密码（至少 8 位）"
                    rules={[
                      { required: true, message: '请输入密码' },
                      { min: 8, message: '密码至少 8 位' },
                    ]}
                  >
                    <Input.Password />
                  </Form.Item>
                  <Form.Item
                    name="displayName"
                    label="昵称（2–20 字）"
                    rules={[{ required: true, message: '请输入昵称' }]}
                  >
                    <Input />
                  </Form.Item>
                  <Form.Item
                    name="email"
                    label="校园邮箱"
                    rules={[{ required: true, message: '请输入校园邮箱' }]}
                  >
                    <Input />
                  </Form.Item>
                  <Form.Item name="college" label="学院（可选）">
                    <Input />
                  </Form.Item>
                  <Form.Item name="className" label="班级（可选）">
                    <Input />
                  </Form.Item>
                  <Button type="primary" htmlType="submit" block>
                    注册并登录
                  </Button>
                </Form>
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}
