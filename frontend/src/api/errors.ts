/** 后端以 RFC 9457 Problem Details 返回错误，这里统一转成带业务码的异常。 */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

export function toApiError(status: number, data: unknown, fallback: string): ApiError {
  if (data && typeof data === 'object') {
    const record = data as Record<string, unknown>;
    const code = typeof record.code === 'string' ? record.code : 'SYS-000';
    const detail = typeof record.detail === 'string' ? record.detail : fallback;
    return new ApiError(status, code, detail);
  }
  return new ApiError(status, 'SYS-000', fallback);
}
