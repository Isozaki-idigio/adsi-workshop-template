# Unit 3: 休暇申請

## 概要

休暇（有給/半休/特別休暇）の申請、残日数管理、申請履歴確認。

## Phase

**Phase B**（Unit 0 完了後、Unit 1/2 と並行実装可能）

## 担当

担当者A（Unit 1 完了後に着手）

## 依存

- Unit 0（Employee Entity, 共通基盤）
- Unit 1 の JWT フィルタが完成するまでは、テスト時に認証をモックする

## ユーザーストーリー

| ID | ストーリー |
|----|-----------|
| US-009 | 社員として、休暇を申請したい |
| US-010 | 社員として、有給の残日数を確認したい |

## API

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/leave/requests | 休暇申請 |
| GET | /api/leave/requests?status= | 自分の休暇申請一覧 |
| GET | /api/leave/balance | 有給残日数取得 |

## テーブル

| テーブル | マイグレーション |
|---------|----------------|
| leave_requests | V4__create_leave_requests.sql |

## 実装スコープ

### Backend

- [ ] V4__create_leave_requests.sql（Flyway）
- [ ] LeaveRequest Entity
- [ ] LeaveRequestRepository（findByEmployeeId, findByStatus 等）
- [ ] LeaveService interface + impl
  - applyLeave: 残日数チェック → 申請作成（status=PENDING）
  - getRemainingLeaveDays: 12 - 承認済み有給の合計日数
  - getRequests: 社員の申請一覧（ステータスフィルタ）
- [ ] LeaveController
- [ ] DTO（LeaveRequestCreateRequest, LeaveRequestResponse, LeaveBalanceResponse）

### Frontend

- [ ] 休暇申請画面（/leave）
  - 残日数表示
  - 申請フォーム（種類, 開始日, 終了日, 理由）
  - 申請履歴テーブル
- [ ] LeaveForm コンポーネント
- [ ] LeaveHistory コンポーネント

### テスト

- [ ] LeaveService — 申請成功/残日数不足エラー/半休の日数計算
- [ ] LeaveRequestRepository — @DataJpaTest
- [ ] LeaveController — @WebMvcTest（201, 400）
- [ ] 休暇申請画面 — コンポーネントテスト

## ビジネスルール

- 有給残日数 = 12.0 - 承認済み有給の合計日数
- 半休 = 0.5日消化
- 残日数不足の場合は申請不可（400エラー）
- 申請時の status は PENDING（承認は Unit 4 で実装）
- 特別休暇は有給残日数を消化しない

## 完了条件

- [ ] 有給・半休・特別休暇が申請できる
- [ ] 残日数が正しく表示される
- [ ] 残日数不足でエラーが返る
- [ ] 申請履歴がステータス別に表示される
