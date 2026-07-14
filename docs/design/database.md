# DB 設計

## ER 図

```
┌──────────────┐         ┌──────────────────┐         ┌──────────────────┐
│ departments  │         │    employees     │         │  time_records    │
├──────────────┤    1  * ├──────────────────┤    1  * ├──────────────────┤
│ id        PK │────────│ id            PK │────────│ id            PK │
│ name         │         │ employee_code    │         │ employee_id   FK │
│ manager_id FK│────┐    │ name             │         │ work_date        │
│ created_at   │    │    │ email            │         │ clock_in         │
│ updated_at   │    │    │ password_hash    │         │ clock_out        │
│ version      │    │    │ department_id FK │         │ created_at       │
└──────────────┘    │    │ role             │         │ updated_at       │
                    │    │ annual_leave_days│         │ version          │
                    │    │ active           │         └──────────────────┘
                    └───│ created_at       │
                         │ updated_at       │
                         │ version          │
                         └────────┬─────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │ 1         * │           * │ 1
          ┌─────────┴────────┐   │   ┌─────────┴──────────────┐
          │  leave_requests  │   │   │  attendance_requests   │
          ├──────────────────┤   │   ├────────────────────────┤
          │ id            PK │   │   │ id                  PK │
          │ employee_id   FK │   │   │ employee_id         FK │
          │ leave_type       │   │   │ time_record_id      FK │
          │ start_date       │   │   │ request_type           │
          │ end_date         │   │   │ work_date              │
          │ days             │   │   │ requested_clock_in     │
          │ reason           │   │   │ requested_clock_out    │
          │ status           │   │   │ reason                 │
          │ approver_id   FK │   │   │ status                 │
          │ approved_at      │   │   │ approver_id         FK │
          │ created_at       │   │   │ approved_at            │
          │ updated_at       │   │   │ created_at             │
          │ version          │   │   │ updated_at             │
          └──────────────────┘   │   │ version                │
                                 │   └────────────────────────┘
                                 │
```

## テーブル定義

### departments

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| name | VARCHAR(100) | NOT NULL, UNIQUE | 部門名 |
| manager_id | BIGINT | FK(employees.id), NULL | 部門管理者 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |

### employees

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| employee_code | VARCHAR(20) | NOT NULL, UNIQUE | ログインID |
| name | VARCHAR(100) | NOT NULL | 氏名 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt |
| department_id | BIGINT | FK(departments.id), NOT NULL | |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'EMPLOYEE' | EMPLOYEE/MANAGER |
| annual_leave_days | DECIMAL(4,1) | NOT NULL, DEFAULT 12.0 | 有給残日数 |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | |
| version | BIGINT | NOT NULL, DEFAULT 0 | |

### time_records

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| employee_id | BIGINT | FK(employees.id), NOT NULL | |
| work_date | DATE | NOT NULL | 勤務日 |
| clock_in | TIMESTAMP | NOT NULL | 出勤時刻 |
| clock_out | TIMESTAMP | NULL | 退勤時刻 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | |
| version | BIGINT | NOT NULL, DEFAULT 0 | |

インデックス:
- `idx_time_records_employee_date` (employee_id, work_date)

### leave_requests

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| employee_id | BIGINT | FK(employees.id), NOT NULL | |
| leave_type | VARCHAR(20) | NOT NULL | PAID/HALF_DAY/SPECIAL |
| start_date | DATE | NOT NULL | |
| end_date | DATE | NOT NULL | |
| days | DECIMAL(4,1) | NOT NULL | 取得日数 |
| reason | VARCHAR(500) | NULL | |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | |
| approver_id | BIGINT | FK(employees.id), NULL | |
| approved_at | TIMESTAMP | NULL | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | |
| version | BIGINT | NOT NULL, DEFAULT 0 | |

インデックス:
- `idx_leave_requests_employee_status` (employee_id, status)
- `idx_leave_requests_approver_status` (approver_id, status)

### attendance_requests

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| employee_id | BIGINT | FK(employees.id), NOT NULL | |
| time_record_id | BIGINT | FK(time_records.id), NULL | 新規追加時はNULL |
| request_type | VARCHAR(20) | NOT NULL | MODIFY/ADD/DELETE |
| work_date | DATE | NOT NULL | |
| requested_clock_in | TIMESTAMP | NOT NULL | |
| requested_clock_out | TIMESTAMP | NULL | |
| reason | VARCHAR(500) | NOT NULL | |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | |
| approver_id | BIGINT | FK(employees.id), NULL | |
| approved_at | TIMESTAMP | NULL | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | |
| version | BIGINT | NOT NULL, DEFAULT 0 | |

インデックス:
- `idx_attendance_requests_employee_status` (employee_id, status)
- `idx_attendance_requests_approver_status` (approver_id, status)

## Flyway マイグレーション計画

| バージョン | 内容 |
|-----------|------|
| V1__create_departments.sql | departments テーブル作成 |
| V2__create_employees.sql | employees テーブル作成 |
| V3__create_time_records.sql | time_records テーブル作成 |
| V4__create_leave_requests.sql | leave_requests テーブル作成 |
| V5__create_attendance_requests.sql | attendance_requests テーブル作成 |
| V6__add_indexes.sql | パフォーマンス用インデックス追加 |
| V7__insert_master_data.sql | 初期マスタデータ（部門等） |
