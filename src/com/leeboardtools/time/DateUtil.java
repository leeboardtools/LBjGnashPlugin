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
}
