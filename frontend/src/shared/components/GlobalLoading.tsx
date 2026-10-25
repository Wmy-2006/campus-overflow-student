import { Progress } from 'antd';
import { useUiStore } from '@/shared/stores/uiStore';

/** 全局加载条：任一请求进行中即显示，由 axios 拦截器维护的计数驱动。 */
export function GlobalLoading() {
  const active = useUiStore((s) => s.pendingRequests > 0);
  if (!active) return null;
  return (
    <div style={{ position: 'fixed', top: 0, left: 0, right: 0, zIndex: 2000, pointerEvents: 'none' }}>
      <Progress
        percent={100}
        showInfo={false}
        status="active"
        strokeColor="#1677ff"
        trailColor="transparent"
        size="small"
      />
    </div>
  );
}
