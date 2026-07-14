# Unit 0: 共通基盤

## 概要

全Unitの前提となるプロジェクト骨格。ビルド・テスト実行・DB接続が動作する状態を作る。

## Phase

**Phase A**（最初に合同で実装）

## 依存

なし（最初に実装する）

## スコープ

### Backend（Spring Boot）

- [ ] プロジェクト初期化（Spring Boot 3.x, Java 21）
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-validation
  - spring-boot-starter-security
  - flyway-core
  - h2（テスト用）
  - postgresql（本番用）
  - lombok
  - jjwt（JWT）
- [ ] パッケージ構成
  ```
  com.example.attendance/
    config/         — SecurityConfig, CorsConfig 等
    common/         — Enum, VO, 例外, DTO基底
    employee/       — Entity, Repository（空でOK）
    department/     — Entity, Repository（空でOK）
  ```
- [ ] Flyway マイグレーション
  - V1__create_departments.sql
  - V2__create_employees.sql
  - V7__insert_master_data.sql（テスト用初期データ）
- [ ] Entity（Department, Employee）
- [ ] Enum（Role, LeaveType, ApprovalStatus, AttendanceRequestType）
- [ ] Value Object（WorkDuration, DateRange）
- [ ] 共通例外ハンドラ（@RestControllerAdvice）
- [ ] SecurityConfig（JWT フィルタの骨格、全エンドポイント permitAll で仮設定）
- [ ] application.yml / application-test.yml（H2）
- [ ] テスト実行確認（`./gradlew test` が通る）
- [ ] ArchUnit テスト（レイヤー依存ルール）

### Frontend（Next.js）

- [ ] プロジェクト初期化（Next.js 14+, TypeScript, Tailwind CSS）
- [ ] ディレクトリ構成
  ```
  src/
    app/            — App Router
    components/     — 共通コンポーネント
    lib/            — API client, 型定義, ユーティリティ
    features/       — 機能別ディレクトリ（この時点では空）
  ```
- [ ] API クライアント基盤（fetch ラッパー + withBasePath）
- [ ] 型定義（Enum 対応型、共通レスポンス型）
- [ ] 共通コンポーネント（NavBar 骨格, StatusBadge）
- [ ] テスト設定（Vitest + Testing Library）
- [ ] テスト実行確認（`npm test` が通る）
- [ ] ESLint / Prettier 設定

### インフラ

- [ ] docker-compose.yml（PostgreSQL + アプリ）
- [ ] .env.example

## テーブル（この Unit で作成）

- departments
- employees

## API（この Unit では公開しない）

なし（共通基盤のみ）

## 完了条件

- [ ] `./gradlew test` が成功する
- [ ] `npm test` が成功する
- [ ] `docker compose up` で PostgreSQL + アプリが起動する
- [ ] Flyway マイグレーションが実行される
- [ ] ArchUnit テストがパスする
