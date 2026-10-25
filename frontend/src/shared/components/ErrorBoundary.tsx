import { Component, type ErrorInfo, type ReactNode } from 'react';
import { Result } from 'antd';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  message: string;
}

/** 全局错误边界：子树渲染异常时兜底，避免整页白屏。 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, message: '' };

  static getDerivedStateFromError(error: unknown): State {
    return {
      hasError: true,
      message: error instanceof Error ? error.message : '页面渲染出现未知错误',
    };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    console.error('ErrorBoundary caught:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Result
          status="error"
          title="页面出错了"
          subTitle={this.state.message}
          extra={
            <a href="/questions" onClick={() => this.setState({ hasError: false, message: '' })}>
              回到问题列表
            </a>
          }
        />
      );
    }
    return this.props.children;
  }
}
