import { Employee } from './types';
import { apiClient } from './api-client';

const TOKEN_KEY = 'attendance_token';

export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function isAuthenticated(): boolean {
  return getToken() !== null;
}

interface LoginResponse {
  token: string;
  employee: Employee;
}

export async function login(employeeCode: string, password: string): Promise<LoginResponse> {
  const response = await apiClient<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ employeeCode, password }),
  });
  setToken(response.token);
  return response;
}

export async function fetchMe(): Promise<Employee> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<Employee>('/auth/me', { token });
}

export function logout(): void {
  clearToken();
  window.location.href = '/login';
}
