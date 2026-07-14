# Unit 1: 認証

## 概要

ID/パスワードによるログイン、JWT トークン発行、ログインユーザー情報取得。

## Phase

**Phase B**（Unit 0 完了後、Unit 2/3 と並行実装可能）

## 担当

担当者A

## 依存

- Unit 0（Employee Entity, SecurityConfig 骨格）

## ユーザーストーリー

| ID | ストーリー |
|----|-----------|
| US-014 | 利用者として、ID/パスワードでログインしたい |

## API

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/auth/login | ログイン（JWT発行） |
| GET | /api/auth/me | ログインユーザー情報取得 |

## 実装スコープ

### Backend

- [ ] EmployeeService.authenticate()
- [ ] JwtTokenProvider（トークン生成・検証）
- [ ] JwtAuthenticationFilter（リクエストごとのトークン検証）
- [ ] SecurityConfig 本設定（エンドポイント別認可ルール）
- [ ] AuthController（login, me）
- [ ] LoginRequest / LoginResponse / EmployeeResponse（record DTO）

### Frontend

- [ ] ログイン画面（/login）
- [ ] 認証状態管理（トークン保持、リダイレクト）
- [ ] API client にトークンヘッダー付与
- [ ] NavBar にユーザー名・ログアウト表示

### テスト

- [ ] EmployeeService.authenticate — 正常系・異常系
- [ ] JwtTokenProvider — 生成・検証・期限切れ
- [ ] AuthController — @WebMvcTest（200, 401）
- [ ] ログイン画面 — コンポーネントテスト

## テーブル

追加なし（Employee は Unit 0 で作成済み）

## 完了条件

- [ ] 正しいID/パスワードでJWTが発行される
- [ ] 不正な認証情報で401が返る
- [ ] JWT付きリクエストで /api/auth/me が200を返す
- [ ] フロントからログイン→ダッシュボード遷移ができる
