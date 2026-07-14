# ドメインモデル設計

## ドメイン概要図

```
┌─────────────┐       ┌─────────────┐       ┌─────────────────┐
│ Department  │1    * │  Employee   │1    * │  TimeRecord     │
│  (部門)     │───────│  (社員)     │───────│  (打刻記録)      │
└─────────────┘       └──────┬──────┘       └─────────────────┘
                             │
                             │ 1    *
                      ┌──────┴──────────────┐
                      │                     │
               ┌──────┴──────┐     ┌────────┴────────┐
               │ LeaveRequest│     │ AttendanceRequest│
               │ (休暇申請)   │     │ (打刻修正申請)    │
               └─────────────┘     └─────────────────┘
```

## Entity

### Department（部門）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK, 自動採番 |
| name | String | 部門名（一意） |
| managerId | Long | 部門管理者の Employee ID（FK） |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |
| version | Long | 楽観ロック |

### Employee（社員）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK, 自動採番 |
| employeeCode | String | 社員コード（一意、ログインID） |
| name | String | 氏名 |
| email | String | メールアドレス |
| passwordHash | String | BCrypt ハッシュ化パスワード |
| departmentId | Long | 所属部門 ID（FK） |
| role | Role | EMPLOYEE / MANAGER |
| annualLeaveDays | BigDecimal | 有給残日数 |
| active | boolean | 有効フラグ |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |
| version | Long | 楽観ロック |

### TimeRecord（打刻記録）

1日に複数レコードを許容（中抜け対応）。

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK, 自動採番 |
| employeeId | Long | 社員 ID（FK） |
| workDate | LocalDate | 勤務日 |
| clockIn | LocalDateTime | 出勤時刻 |
| clockOut | LocalDateTime | 退勤時刻（nullable: 未退勤） |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |
| version | Long | 楽観ロック |

ビジネスルール:
- clockOut が null = 勤務中
- 同一 employeeId + workDate に複数レコード可
- clockIn < clockOut（退勤は出勤より後）

### LeaveRequest（休暇申請）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK, 自動採番 |
| employeeId | Long | 申請者の社員 ID（FK） |
| leaveType | LeaveType | PAID / HALF_DAY / SPECIAL |
| startDate | LocalDate | 休暇開始日 |
| endDate | LocalDate | 休暇終了日 |
| days | BigDecimal | 取得日数（半休=0.5） |
| reason | String | 申請理由 |
| status | ApprovalStatus | PENDING / APPROVED / REJECTED |
| approverId | Long | 承認者の社員 ID（FK, nullable） |
| approvedAt | LocalDateTime | 承認日時（nullable） |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |
| version | Long | 楽観ロック |

### AttendanceRequest（打刻修正申請）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK, 自動採番 |
| employeeId | Long | 申請者の社員 ID（FK） |
| timeRecordId | Long | 対象の打刻記録 ID（FK, nullable: 新規追加時） |
| requestType | AttendanceRequestType | MODIFY / ADD / DELETE |
| workDate | LocalDate | 対象日 |
| requestedClockIn | LocalDateTime | 修正後の出勤時刻 |
| requestedClockOut | LocalDateTime | 修正後の退勤時刻 |
| reason | String | 申請理由 |
| status | ApprovalStatus | PENDING / APPROVED / REJECTED |
| approverId | Long | 承認者の社員 ID（FK, nullable） |
| approvedAt | LocalDateTime | 承認日時（nullable） |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |
| version | Long | 楽観ロック |

## Value Object

### WorkDuration（勤務時間）

```java
public record WorkDuration(Duration duration) {
    public static final Duration STANDARD_HOURS = Duration.ofMinutes(435); // 7h15m

    public Duration overtimeMinutes() {
        if (duration.compareTo(STANDARD_HOURS) > 0) {
            return duration.minus(STANDARD_HOURS);
        }
        return Duration.ZERO;
    }

    public boolean isNightWork(LocalDateTime clockIn, LocalDateTime clockOut) {
        // 22:00〜05:00 の範囲に勤務が含まれるか
    }
}
```

### DateRange（日付範囲）

```java
public record DateRange(LocalDate start, LocalDate end) {
    public DateRange {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start must be before or equal to end");
        }
    }

    public long days() {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}
```

## Enum

### Role

```java
public enum Role { EMPLOYEE, MANAGER }
```

### LeaveType

```java
public enum LeaveType { PAID, HALF_DAY, SPECIAL }
```

### ApprovalStatus

```java
public enum ApprovalStatus { PENDING, APPROVED, REJECTED }
```

### AttendanceRequestType

```java
public enum AttendanceRequestType { MODIFY, ADD, DELETE }
```

## Service

### AttendanceService

- `clockIn(employeeId)` — 出勤打刻
- `clockOut(employeeId)` — 退勤打刻
- `getTimeRecords(employeeId, yearMonth)` — 月別打刻一覧
- `getDailyWorkDuration(employeeId, date)` — 日別勤務時間（中抜け合算）
- `getMonthlyWorkSummary(employeeId, yearMonth)` — 月次集計

### LeaveService

- `applyLeave(employeeId, request)` — 休暇申請
- `approveLeave(approverId, requestId)` — 休暇承認
- `rejectLeave(approverId, requestId)` — 休暇却下
- `getRemainingLeaveDays(employeeId)` — 有給残日数取得

### AttendanceRequestService

- `submitRequest(employeeId, request)` — 打刻修正申請
- `approveRequest(approverId, requestId)` — 承認（打刻に反映）
- `rejectRequest(approverId, requestId)` — 却下

### EmployeeService

- `authenticate(employeeCode, password)` — 認証
- `getByDepartment(departmentId)` — 部門別社員一覧
- `getById(employeeId)` — 社員情報取得

## Repository（interface）

- `EmployeeRepository`
- `DepartmentRepository`
- `TimeRecordRepository`
- `LeaveRequestRepository`
- `AttendanceRequestRepository`
