const BASE_PATH = process.env.NEXT_PUBLIC_BASE_PATH || '';

export function withBasePath(path: string): string {
  return `${BASE_PATH}${path}`;
}

interface FetchOptions extends RequestInit {
  token?: string;
}

export async function apiClient<T>(
  path: string,
  options: FetchOptions = {}
): Promise<T> {
  const { token, headers: customHeaders, ...rest } = options;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((customHeaders as Record<string, string>) || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(withBasePath(`/api${path}`), {
    headers,
    ...rest,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'エラーが発生しました' }));
    throw new ApiError(response.status, error.message || 'エラーが発生しました', error.code);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly code?: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}
