package com.example.attendance.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkDurationTest {

    @Test
    @DisplayName("所定時間以下の場合、残業は0分")
    void overtime_underStandard_returnsZero() {
        var duration = new WorkDuration(Duration.ofHours(7));

        assertThat(duration.overtimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("所定時間（7h15m）超過分が残業として算出される")
    void overtime_overStandard_returnsOvertime() {
        var duration = new WorkDuration(Duration.ofHours(8));

        assertThat(duration.overtimeMinutes()).isEqualTo(45);
    }

    @Test
    @DisplayName("所定時間ちょうどの場合、残業は0分")
    void overtime_exactlyStandard_returnsZero() {
        var duration = new WorkDuration(Duration.ofMinutes(435));

        assertThat(duration.overtimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("合計分数が正しく返される")
    void totalMinutes_returnsCorrectValue() {
        var duration = new WorkDuration(Duration.ofHours(3).plusMinutes(30));

        assertThat(duration.totalMinutes()).isEqualTo(210);
    }

    @Test
    @DisplayName("負の時間はエラーになる")
    void constructor_negativeDuration_throwsException() {
        assertThatThrownBy(() -> new WorkDuration(Duration.ofMinutes(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("nullはエラーになる")
    void constructor_null_throwsException() {
        assertThatThrownBy(() -> new WorkDuration(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("22:00以降の勤務は深夜勤務と判定される")
    void isNightWork_after22_returnsTrue() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 22, 30);
        var clockOut = LocalDateTime.of(2026, 7, 14, 23, 30);

        assertThat(WorkDuration.isNightWork(clockIn, clockOut)).isTrue();
    }

    @Test
    @DisplayName("日中の勤務は深夜勤務ではない")
    void isNightWork_daytime_returnsFalse() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 17, 0);

        assertThat(WorkDuration.isNightWork(clockIn, clockOut)).isFalse();
    }

    @Test
    @DisplayName("日付をまたぐ勤務は深夜勤務と判定される")
    void isNightWork_crossMidnight_returnsTrue() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 20, 0);
        var clockOut = LocalDateTime.of(2026, 7, 15, 2, 0);

        assertThat(WorkDuration.isNightWork(clockIn, clockOut)).isTrue();
    }
}
