# Unit 5: 集計・エクスポート

## 概要

月次労働時間集計、CSV/PDFエクスポート。MVP外だが次期リリース候補。

## Phase

**Phase C**（Unit 2 完了後）

## 担当

担当者A（Unit 3 完了後に着手）

## 依存

- Unit 2（TimeRecord, AttendanceService.getMonthlyWorkSummary）

## ユーザーストーリー

| ID | ストーリー |
|----|-----------|
| US-012 | 社員として、月次の労働時間合計を確認したい |
| US-013 | 管理者として、勤怠データをCSV/PDFで出力したい |

## API

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/attendance/records?yearMonth= | 月次集計付き（Unit 2 を拡張） |
| GET | /api/admin/export?yearMonth=&format=csv\|pdf | エクスポート |

## テーブル

追加なし（既存の time_records を集計）

## 実装スコープ

### Backend

- [ ] AttendanceService.getMonthlyWorkSummary の拡張（残業・深夜の分類）
- [ ] ExportService interface + impl
  - exportCsv: 月次勤怠データを CSV 形式で出力
  - exportPdf: 月次勤怠データを PDF 形式で出力（Apache PDFBox 等）
- [ ] AdminController.exportAttendance 追加
- [ ] 深夜勤務判定ロジック（22:00〜05:00 の時間帯に重なる分を算出）

### Frontend

- [ ] 勤怠一覧に月次集計サマリ追加（合計時間, 残業時間）
- [ ] エクスポートボタン（管理者画面に追加）
- [ ] ダウンロード処理（Blob → リンク）

### テスト

- [ ] 月次集計 — 残業/深夜の分類が正しいこと
- [ ] ExportService — CSV 内容検証
- [ ] エクスポート API — @WebMvcTest（200 + Content-Type）

## ビジネスルール

- 月間労働時間 = 全日の勤務時間合計
- 月間残業時間 = 全日の (勤務時間 - 7.25h) の合計（マイナスは0）
- 深夜時間 = 22:00〜05:00 に含まれる勤務時間

## 完了条件

- [ ] 月次集計画面に合計・残業時間が表示される
- [ ] CSV ダウンロードで正しいデータが出力される
- [ ] PDF ダウンロードが成功する
