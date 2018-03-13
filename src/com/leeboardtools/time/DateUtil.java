/*
 * Copyright 2018 Albert Santos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.leeboardtools.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import javafx.util.StringConverter;

/**
 * Some handy dandy date manipulation routines...
 * @author Albert Santos
 */
public class DateUtil {
    
    private static DayOfWeek defaultFirstDayOfWeek = DayOfWeek.SUNDAY;
    
    /**
     * @return A default value to use for the first day of the week.
     */
    public static DayOfWeek getDefaultFirstDayOfWeek() {
        return defaultFirstDayOfWeek;
    }
    
    /**
     * Sets the default day of the week.
     * @param dayOfWeek The day of the week.
     */
    public static void setDefaultFirstDayOfWeek(DayOfWeek dayOfWeek) {
        defaultFirstDayOfWeek = dayOfWeek;
    }
    
    /**
     * Retrieves a non-<code>null</code> first day of the week.
     * @param dayOfWeek The potential day of the week, used if not <code>null</code>.
     * @return dayOfWeek if it is not <code>null</code>, the result of {@link #getDefaultFirstDayOfWeek() }
     * if it is.
     */
    public static DayOfWeek getValidFirstDayOfWeek(DayOfWeek dayOfWeek) {
        return (dayOfWeek == null) ? defaultFirstDayOfWeek : dayOfWeek;
    }
    
    
    /**
     * Retrieves a {@link LocalDate} that's a specified number of days from the start of the
     * month of a reference date.
     * @param refDate   The reference date.
     * @param dayCount  The number of days.
     * @return The date.
     */
    public static LocalDate getOffsetFromStartOfMonth(LocalDate refDate, int dayCount) {
        int deltaDays = 1 - refDate.getDayOfMonth() + dayCount;
        return refDate.plusDays(deltaDays);
    }
    
    /**
     * Retrieves a {@link LocalDate} that's the first day of the month of a reference date.
     * @param refDate   The reference date.
     * @return The date that's the first of the month.
     */
    public static LocalDate getStartOfMonth(LocalDate refDate) {
        return getOffsetFromStartOfMonth(refDate, 0);
    }
    
    /**
     * Retrieves a {@link LocalDate} that's a specified number of days back from the last day
     * of the month of a reference date.
     * @param refDate   The reference date.
     * @param dayCount  The number of days, positive values are into the past.
     * @return The date.
     */
    public static LocalDate getOffsetFromEndOfMonth(LocalDate refDate, int dayCount) {
        LocalDate nextMonthDate = refDate.plusMonths(1);
        return getOffsetFromStartOfMonth(nextMonthDate, -1 - dayCount);
    }
    
    /**
     * Retrieves a {@link LocalDate} that's the last day of the month of a reference date.
     * @param refDate
     * @return 
     */
    public static LocalDate getEndOfMonth(LocalDate refDate) {
        return getOffsetFromEndOfMonth(refDate, 0);
    }
    
    
    /**
     * Retrieves a {@link LocalDate} that's a specified number of days from the first day of
     * the quarter of a reference date.
     * @param refDate   The reference date.
     * @param dayCount  The number of days.
     * @return The date.
     */
    public static LocalDate getOffsetFromStartOfQuarter(LocalDate refDate, int dayCount) {
        return getStartOfQuarter(refDate).plusDays(dayCount);
    }
    
    /**
     * Retrieves the first day of the quarter of a reference date.
     * @param refDate   The reference date.
     * @return  The first day of the quarter containing refDate.
     */
    public static LocalDate getStartOfQuarter(LocalDate refDate) {
        Quarter quarter = Quarter.of(refDate);
        return LocalDate.of(refDate.getYear(), quarter.getFirstMonth(), 1);
    }
    
    /**
     * Retrieves a {@link LocalDate} that's a specified number of days from the last day of
     * the quarter of a reference date.
     * @param refDate   The reference date.
     * @param dayCount  The number of days.
     * @return The date.
     */
    public static LocalDate getOffsetFromEndOfQuarter(LocalDate refDate, int dayCount) {
        Quarter quarter = Quarter.of(refDate);
        LocalDate lastMonthDate = LocalDate.of(refDate.getYear(), quarter.getLastMonth(), 1);
        return getOffsetFromEndOfMonth(lastMonthDate, dayCount);
    }
    
    /**
     * Retrieves the last day of the quarter of a reference date.
     * @param refDate   The reference date.
     * @return The last day of the quarter containing refDate.
     */
    public static LocalDate getEndOfQuarter(LocalDate refDate) {
        Quarter quarter = Quarter.of(refDate);
        LocalDate lastMonthDate = LocalDate.of(refDate.getYear(), quarter.getLastMonth(), 1);
        return getEndOfMonth(lastMonthDate);
    }
    
    
    /**
     * Retrieves a {@link LocalDate} that's a specified number of days from the first day of
     * the year of a reference date.
     * @param refDate   The reference date.
     * @param dayCount  The number of days.
     * @return The date.
     */
    public static LocalDate getOffsetFromStartOfYear(LocalDate refDate, int dayCount) {
        return LocalDate.of(refDate.getYear(), Month.JANUARY, 1).plusDays(dayCount);
    }
    
    /**
     * Retrieves a {@link LocalDate} that's the first day of the year of a reference date.
     * @param refDate   The reference date.
     * @return The first day of the year of the reference date.
     */
    public static LocalDate getStartOfYear(LocalDate refDate) {
        return LocalDate.of(refDate.getYear(), Month.JANUARY, 1);
    }
    
    /**
     * Retrieves a {@link LocalDate} that's a specified number of days before the last day of
     * the year of a reference date.
     * @param refDate   The reference date.
     * @param dayCount  The number of days, positive is in the past.
     * @return The date.
     */
    public static LocalDate getOffsetFromEndOfYear(LocalDate refDate, int dayCount) {
        return LocalDate.of(refDate.getYear(), Month.DECEMBER, 31).minusDays(dayCount);
    }
    
    /**
     * Retrieves a {@link LocalDate} that's the last day of the year of a reference date.
     * @param refDate   The reference date.
     * @return The last day of the year of the reference date.
     */
    public static LocalDate getEndOfYear(LocalDate refDate) {
        return LocalDate.of(refDate.getYear(), Month.DECEMBER, 31);
    }
    
    
    /**
     * Retrieves a {@link LocalDate} that falls on a given {@link DayOfWeek} and is either the same as
     * or the closest day before a reference date.
     * @param refDate   The reference date.
     * @param dayOfWeek The day of week.
     * @return The date.
     */
    public static LocalDate getClosestDayOfWeekOnOrBefore(LocalDate refDate, DayOfWeek dayOfWeek) {
        int deltaDays = refDate.getDayOfWeek().getValue() - dayOfWeek.getValue();
        if (deltaDays < 0) {
            deltaDays += 7;
        }
        return refDate.minusDays(deltaDays);
    }
    
    
    /**
     * Retrieves a {@link LocalDate} that falls on a given {@link DayOfWeek} and is either the same as
     * or the closest day after a reference date.
     * @param refDate   The reference date.
     * @param dayOfWeek The day of week.
     * @return The date.
     */
    public static LocalDate getClosestDayOfWeekOnOrAfter(LocalDate refDate, DayOfWeek dayOfWeek) {
        int deltaDays = dayOfWeek.getValue() - refDate.getDayOfWeek().getValue();
        if (deltaDays < 0) {
            deltaDays += 7;
        }
        return refDate.plusDays(deltaDays);
    }
    
    
    /**
     * Returns a copy of a reference date with a specified number of quarters added.
     * @param refDate   The reference date.
     * @param quarters  The number of quarters.
     * @return refDate with quarters quarters added.
     */
    public static LocalDate plusQuarters(LocalDate refDate, int quarters) {
        return refDate.plusMonths(quarters * 3);
    }
    
    /**
     * Returns a copy of a reference date with a specified number of quarters subtracted.
     * @param refDate   The reference date.
     * @param quarters  The number of quarters.
     * @return refDate with quarters quarters subtracted.
     */
    public static LocalDate minusQuarters(LocalDate refDate, int quarters) {
        return refDate.minusMonths(quarters * 3);
    }
    
    /**
     * Returns the number of days to go from one date to another date.
     * @param refDate   The reference date.
     * @param destDate  The date to go to.
     * @return The number of days, positive if destDate is after refDate, negative if destDate is before refDate.
     */
    public static long daysTo(LocalDate refDate, LocalDate destDate) {
        return refDate.until(destDate, ChronoUnit.DAYS);
    }
    
    
    /**
     * String converter for {@link DayOfWeek}.
     * This calls {@link DayOfWeek#getDisplayName(java.time.format.TextStyle, java.util.Locale) }
     * to obtain the string representation.
     */
    public static class DayOfWeekStringConverter extends StringConverter<DayOfWeek> {
        private final TextStyle textStyle;
        private final Locale locale;

        public DayOfWeekStringConverter(TextStyle style, Locale locale) {
            this.textStyle = (style == null) ? TextStyle.FULL_STANDALONE : style;
            this.locale = (locale == null) ? Locale.getDefault() : locale;
        }
        public DayOfWeekStringConverter() {
            this(null, null);
        }
        public DayOfWeekStringConverter(TextStyle style) {
            this(style, null);
        }

        @Override
        public String toString(DayOfWeek object) {
            return object.getDisplayName(textStyle, locale);
        }

        @Override
        public DayOfWeek fromString(String string) {
            for (int i = 1; i <= 7; ++i) {
                DayOfWeek dayOfWeek = DayOfWeek.of(i);
                String displayName = toString(dayOfWeek);
                if (displayName.equals(string)) {
                    return dayOfWeek;
                }
            }
            return null;
        }
        
    }
    
    
    /**
     * Retrieves the time between two dates, in years, including fractions of a year.
     * Currently this is fairly simple, it only takes the number of days and divides by 365.25.
     * @param baseDate  The reference date.
     * @param toDate    The 'to' date.
     * @return The number of years, including fraction. &gt; 0 if toDate is after baseDate.
     */
    public static double getYearsUntil(LocalDate baseDate, LocalDate toDate) {
        // TODO Need to handle leap years...
        long deltaDays = baseDate.until(toDate, ChronoUnit.DAYS);
        return deltaDays / 365.25;
    }
}
