# Unit 4: 管理者機能

## 概要

部門勤怠の閲覧・修正、休暇申請・打刻修正申請の承認/却下。管理者画面一式。

## Phase

**Phase C**（Unit 1, 2, 3 完了後）

## 担当

担当者B（Unit 2 完了後に着手）

## 依存

- Unit 1（認証・ロール判定）
- Unit 2（TimeRecord, AttendanceService）
- Unit 3（LeaveRequest, LeaveService）

## ユーザーストーリー

| ID | ストーリー |
|----|-----------|
| US-004 | 社員として、打刻修正を申請したい |
| US-005 | 管理者として、打刻修正申請を承認/却下したい |
| US-007 | 管理者として、部門の勤怠一覧を確認したい |
| US-008 | 管理者として、部下の勤怠を修正したい |
| US-011 | 管理者として、部下の休暇申請を承認/却下したい |

## API

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/attendance-requests | 打刻修正申請（社員） |
| GET | /api/attendance-requests | 自分の打刻修正申請一覧 |
| GET | /api/admin/department/attendance?yearMonth=&employeeId= | 部門勤怠一覧 |
| PUT | /api/admin/attendance/{employeeId}/{recordId} | 部下の勤怠直接修正 |
| GET | /api/admin/pending-requests | 承認待ち一覧 |
| POST | /api/admin/leave-requests/{id}/approve | 休暇承認 |
| POST | /api/admin/leave-requests/{id}/reject | 休暇却下 |
| POST | /api/admin/attendance-requests/{id}/approve | 打刻修正承認 |
| POST | /api/admin/attendance-requests/{id}/reject | 打刻修正却下 |

## テーブル

| テーブル | マイグレーション |
|---------|----------------|
| attendance_requests | V5__create_attendance_requests.sql |

## 実装スコープ

### Backend

- [ ] V5__create_attendance_requests.sql（Flyway）
- [ ] AttendanceRequest Entity
- [ ] AttendanceRequestRepository
- [ ] AttendanceRequestService interface + impl
  - submitRequest: 修正申請作成
  - approveRequest: 承認 → TimeRecord に反映
  - rejectRequest: 却下
- [ ] LeaveService に承認/却下メソッド追加
  - approveLeave: status=APPROVED, 残日数減算
  - rejectLeave: status=REJECTED
- [ ] AdminController
  - getDepartmentAttendance: 部門員を取得 → 勤怠集計
  - modifyEmployeeAttendance: TimeRecord 直接更新
  - getPendingRequests: 自部門の PENDING 申請
  - approve/reject 各種
- [ ] 権限チェック: 管理者ロール + 自部門の社員のみ操作可能
- [ ] AttendanceRequestController（社員側の申請操作）

### Frontend

- [ ] 管理者ダッシュボード（/admin）
- [ ] 部門勤怠一覧（/admin/attendance）
- [ ] 承認一覧（/admin/approvals）
- [ ] 打刻修正申請画面（/attendance-request）— 社員側
- [ ] ApprovalCard コンポーネント
- [ ] AttendanceTable コンポーネント（管理者用）
- [ ] ロールに応じた NavBar メニュー切替

### テスト

- [ ] AttendanceRequestService — 申請/承認→反映/却下
- [ ] LeaveService.approve/reject — 残日数更新の検証
- [ ] AdminController — 権限チェック（403: 他部門/非管理者）
- [ ] 統合テスト — 申請→承認→TimeRecord反映の一連フロー
- [ ] 承認一覧 — コンポーネントテスト

## ビジネスルール

- 管理者は自部門の社員の操作のみ可能（他部門 → 403）
- 承認者 = 申請者の所属部門の manager
- 休暇承認時: status=APPROVED, 有給の場合は残日数を減算
- 打刻修正承認時: TimeRecord を申請内容で上書き（ADD の場合は新規作成）
- 既に承認/却下済みの申請は再操作不可（409）

## 完了条件

- [ ] 管理者が部門の勤怠一覧を確認できる
- [ ] 管理者が打刻を直接修正できる
- [ ] 社員が打刻修正を申請でき、管理者が承認/却下できる
- [ ] 管理者が休暇申請を承認/却下でき、残日数が更新される
- [ ] 他部門の操作が403で拒否される
