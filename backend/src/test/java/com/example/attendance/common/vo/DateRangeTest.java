package com.example.attendance.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateRangeTest {

    @Test
    @DisplayName("正常な日付範囲が作成できる")
    void constructor_validRange_succeeds() {
        var range = new DateRange(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 5));

        assertThat(range.start()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(range.end()).isEqualTo(LocalDate.of(2026, 7, 5));
    }

    @Test
    @DisplayName("同日の範囲が作成できる")
    void constructor_sameDay_succeeds() {
        var date = LocalDate.of(2026, 7, 14);
        var range = new DateRange(date, date);

        assertThat(range.days()).isEqualTo(1);
    }

    @Test
    @DisplayName("開始日が終了日より後の場合エラー")
    void constructor_startAfterEnd_throwsException() {
        assertThatThrownBy(() -> new DateRange(
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start must be before or equal to end");
    }

    @Test
    @DisplayName("nullの場合エラー")
    void constructor_null_throwsException() {
        assertThatThrownBy(() -> new DateRange(null, LocalDate.of(2026, 7, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("日数が正しく計算される")
    void days_returnsCorrectCount() {
        var range = new DateRange(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 5));

        assertThat(range.days()).isEqualTo(5);
    }
}
