# Unit 2: 打刻・勤怠

## 概要

出退勤打刻（中抜け対応）、自分の勤怠一覧表示、打刻状態確認。

## Phase

**Phase B**（Unit 0 完了後、Unit 1/3 と並行実装可能）

## 担当

担当者B

## 依存

- Unit 0（Employee Entity, 共通基盤）
- Unit 1 の JWT フィルタが完成するまでは、テスト時に認証をモックする

## ユーザーストーリー

| ID | ストーリー |
|----|-----------|
| US-001 | 社員として、出勤を打刻したい |
| US-002 | 社員として、退勤を打刻したい |
| US-003 | 社員として、中抜けを記録したい |
| US-006 | 社員として、自分の勤怠一覧を確認したい |

## API

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/attendance/clock-in | 出勤打刻 |
| POST | /api/attendance/clock-out | 退勤打刻 |
| GET | /api/attendance/status | 現在の打刻状態 |
| GET | /api/attendance/records?yearMonth=YYYY-MM | 月別勤怠一覧 |

## テーブル

| テーブル | マイグレーション |
|---------|----------------|
| time_records | V3__create_time_records.sql |

## 実装スコープ

### Backend

- [ ] V3__create_time_records.sql（Flyway）
- [ ] TimeRecord Entity
- [ ] TimeRecordRepository（findByEmployeeIdAndWorkDate 等）
- [ ] AttendanceService interface + impl
  - clockIn: 未退勤レコードがなければ新規作成
  - clockOut: 未退勤レコードに退勤時刻を記録
  - getStatus: 当日のレコード一覧 + 勤務中かどうか
  - getRecords: 月別の日ごと集計
- [ ] WorkDuration で日別勤務時間を計算（中抜け合算）
- [ ] AttendanceController
- [ ] DTO（TimeRecordResponse, AttendanceStatusResponse, MonthlyAttendanceResponse, DailyAttendanceResponse）

### Frontend

- [ ] ダッシュボード画面（/）— 打刻ボタン + 本日の記録
- [ ] 勤怠一覧画面（/attendance）— 月別一覧テーブル
- [ ] ClockButton コンポーネント
- [ ] TimeRecordList コンポーネント
- [ ] MonthSelector コンポーネント

### テスト

- [ ] AttendanceService — 出勤/退勤/中抜け/二重出勤エラー
- [ ] WorkDuration — 勤務時間計算、残業判定、深夜判定
- [ ] TimeRecordRepository — @DataJpaTest
- [ ] AttendanceController — @WebMvcTest（201, 409, 404）
- [ ] ダッシュボード — コンポーネントテスト

## ビジネスルール

- 未退勤のレコードがある状態で出勤打刻 → 409 エラー
- 出勤レコードがない状態で退勤打刻 → 404 エラー
- 1日の勤務時間 = 全レコードの (clockOut - clockIn) 合計
- 所定 7.25h 超過分が残業

## 完了条件

- [ ] 出勤→退勤→再出勤（中抜け）が正常に動作する
- [ ] 勤怠一覧で月別の日ごと記録が表示される
- [ ] 勤務時間が中抜け含め正しく合算される
- [ ] 二重出勤がエラーになる
