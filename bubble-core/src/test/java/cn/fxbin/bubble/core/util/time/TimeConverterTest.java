package cn.fxbin.bubble.core.util.time;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


/**
 * TimeConverterTest
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/11 10:06
 */
public class TimeConverterTest {



    @Test
    void testConvertToTimestampWithNull() {
        assertNull(TimeConverter.convertToTimestamp(null));
    }

    @Test
    void testConvertToTimestampWithLong() {
        Long timestamp = System.currentTimeMillis();
        assertEquals(timestamp, TimeConverter.convertToTimestamp(timestamp));
    }

    @Test
    void testConvertToTimestampWithLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 7, 15, 12, 0, 0);
        Long expected = DateUtils.toEpochMilli(dateTime);
        assertEquals(expected, TimeConverter.convertToTimestamp(dateTime));
    }

    @Test
    void testConvertToTimestampWithLocalDate() {
        LocalDate date = LocalDate.of(2024, 7, 15);
        Long expected = DateUtils.toEpochMilli(date);
        assertEquals(expected, TimeConverter.convertToTimestamp(date));
    }

    @Test
    void testConvertToTimestampWithDate() {
        Date date = new Date();
        Long expected = date.getTime();
        assertEquals(expected, TimeConverter.convertToTimestamp(date));
    }

    @Test
    void testConvertToTimestampWithInstant() {
        Instant instant = Instant.now();
        Long expected = DateUtils.toEpochMilli(instant);
        assertEquals(expected, TimeConverter.convertToTimestamp(instant));
    }

    @Test
    void testConvertToTimestampWithZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, ZoneId.systemDefault());
        Long expected = DateUtils.toEpochMilli(zonedDateTime);
        assertEquals(expected, TimeConverter.convertToTimestamp(zonedDateTime));
    }

    @Test
    void testConvertToTimestampWithStringTimestamp() {
        Long timestamp = System.currentTimeMillis();
        assertEquals(timestamp, TimeConverter.convertToTimestamp(timestamp.toString()));
    }

    @Test
    void testConvertToTimestampWithStringStandardFormat() {
        String dateTimeStr = "2024-07-15 12:00:00";
        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 15, 12, 0, 0);
        Long expected = DateUtils.toEpochMilli(expectedDateTime);
        assertEquals(expected, TimeConverter.convertToTimestamp(dateTimeStr));
    }

    @Test
    void testConvertToTimestampWithIsoFormat() {
        String dateTimeStr = "2024-07-15T12:00:00";
        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 15, 12, 0, 0);
        Long expected = DateUtils.toEpochMilli(expectedDateTime);
        assertEquals(expected, TimeConverter.convertToTimestamp(dateTimeStr));
    }

    @Test
    void testConvertToTimestampWithIsoFormatWithMillis() {
        String dateTimeStr = "2024-07-15T12:00:00.123";
        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 15, 12, 0, 0, 123_000_000);
        Long expected = DateUtils.toEpochMilli(expectedDateTime);
        assertEquals(expected, TimeConverter.convertToTimestamp(dateTimeStr));
    }

    @Test
    void testConvertToTimestampWithDateOnly() {
        String dateStr = "2024-07-15";
        LocalDate expectedDate = LocalDate.of(2024, 7, 15);
        Long expected = DateUtils.toEpochMilli(expectedDate);
        assertEquals(expected, TimeConverter.convertToTimestamp(dateStr));
    }

    @Test
    void testConvertToTimestampWithSlashFormat() {
        String dateTimeStr = "2024/07/15 12:00:00";
        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 7, 15, 12, 0, 0);
        Long expected = DateUtils.toEpochMilli(expectedDateTime);
        assertEquals(expected, TimeConverter.convertToTimestamp(dateTimeStr));
    }

    @Test
    void testConvertToTimestampWithUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> {
            TimeConverter.convertToTimestamp(123); // Integer is not supported
        });
    }

    @Test
    void testConvertToTimestampWithInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> {
            TimeConverter.convertToTimestamp("invalid-date");
        });
    }

    @Test
    void testConvertToTimestampWithEmptyString() {
        assertNull(TimeConverter.convertToTimestamp(""));
        assertNull(TimeConverter.convertToTimestamp("   "));
    }


}
