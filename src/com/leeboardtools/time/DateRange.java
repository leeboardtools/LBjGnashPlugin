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
import java.util.Objects;

/**
 * Defines a contiguous range of dates.
 * @author Albert Santos
 */
public class DateRange {
    private final LocalDate oldestDate;
    private final LocalDate newestDate;
    
    public DateRange(LocalDate dateA, LocalDate dateB) {
        if (dateA.isAfter(dateB)) {
            this.oldestDate = dateB;
            this.newestDate = dateA;
        }
        else {
            this.oldestDate = dateA;
            this.newestDate = dateB;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.oldestDate);
        hash = 53 * hash + Objects.hashCode(this.newestDate);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DateRange other = (DateRange) obj;
        if (!Objects.equals(this.oldestDate, other.oldestDate)) {
            return false;
        }
        if (!Objects.equals(this.newestDate, other.newestDate)) {
            return false;
        }
        return true;
    }
    
    
    
    /**
     * @return The oldest date in the range.
     */
    public final LocalDate getOldestDate() {
        return oldestDate;
    }
    
    /**
     * @return The newest date in the range, inclusive.
     */
    public final LocalDate getNewestDate() {
        return newestDate;
    }
    
    /**
     * Determines if a date is within the range. A date is within the range if
     * it is on or after the oldest date and before or on the newest date.
     * @param date  The date of interest.
     * @return <code>true</code> if date is in the range.
     */
    public final boolean isDateInRange(LocalDate date) {
        return !oldestDate.isAfter(date) && !newestDate.isBefore(date);
    }
    
    
    /**
     * Throws an {@link IllegalArgumentException} if the count is not a positive value.
     * @param count The count.
     * @throws IllegalArgumentException if count is &le; 0.
     */
    public static void requirePositive(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("The argument must be positive.");
        }
    }
    
    
    /**
     * Returns a range that has a {@link DateOffset} applied to both its oldest and newest dates.
     * @param dateOffset    The date offset, may be <code>null</code>
     * @return The adjusted range, may be the same as this.
     */
    public DateRange applyDateOffset(DateOffset dateOffset) {
        if (dateOffset != null) {
            LocalDate dateA = dateOffset.getOffsetDate(oldestDate);
            LocalDate dateB = dateOffset.getOffsetDate(newestDate);
            return new DateRange(dateA, dateB);
        }
        return this;
    }
    

    /**
     * Interface for objects that generate a date range given a reference date.
     */
    public interface Generator {
        public DateRange generateRange(LocalDate refDate);
    }
    
    
    /**
     * Standard range generator types that can be created by {@link #fromStandard(com.leeboardtools.time.DateRange.Standard, int[]) }.
     */
    public static enum Standard {
        CURRENT_DATE,
        CURRENT_YEAR,
        CURRENT_MONTH,
        CURRENT_QUARTER,
        CURRENT_WEEK,
        YEAR_TO_DATE,
        QUARTER_TO_DATE,
        MONTH_TO_DATE,
        LAST_YEARS,
        LAST_MONTHS,
        LAST_WEEKS,
        LAST_DAYS,
        PRECEEDING_YEARS,
        PRECEEDING_MONTHS,
        PRECEEDING_QUARTERS,
    }
    
    /**
     * Abstract class for the standard generators, this provides methods for obtaining
     * the standard and the array of parameters so a similar generator can be recreated
     * via {@link #fromStandard(com.leeboardtools.time.DateRange.Standard, int[]) }.
     */
    public static abstract class StandardGenerator implements Generator {
        public abstract Standard getStandard();
        public abstract int [] getParameters();
    }
    
    /**
     * Retrieves a generator for a given standard and parameters.
     * @param standard  The standard.
     * @param parameters    The array of parameters.
     * @return The generator.
     */
    public static StandardGenerator fromStandard(Standard standard, int [] parameters) {
        switch (standard) {
            case CURRENT_DATE :
                return new CurrentDate(parameters);
            case CURRENT_YEAR :
                return new CurrentYear(parameters);
            case CURRENT_MONTH :
                return new CurrentMonth(parameters);
            case CURRENT_QUARTER :
                return new CurrentQuarter(parameters);
            case CURRENT_WEEK :
                return new CurrentWeek(parameters);
                
            case YEAR_TO_DATE :
                return new YearToDate(parameters);
            case QUARTER_TO_DATE :
                return new QuarterToDate(parameters);
            case MONTH_TO_DATE :
                return new MonthToDate(parameters);
            case LAST_YEARS :
                return new LastYears(parameters);
            case LAST_MONTHS :
                return new LastMonths(parameters);
            case LAST_WEEKS :
                return new LastWeeks(parameters);
            case LAST_DAYS :
                return new LastDays(parameters);
            case PRECEEDING_YEARS :
                return new PreceedingYears(parameters);
            case PRECEEDING_MONTHS :
                return new PreceedingMonths(parameters);
            case PRECEEDING_QUARTERS :
                return new PreceedingQuarters(parameters);
        }
        
        throw new IllegalArgumentException("standard is invalid.");
    }
    
    
    /**
     * Retrieves a date range defined by two dates.
     * @param dateA The first date.
     * @param dateB The second date.
     * @return The date range.
     */
    public static DateRange fromEdgeDates(LocalDate dateA, LocalDate dateB) {
        return new DateRange(dateA, dateB);
    }
    
    
    static abstract class AbstractToDate extends StandardGenerator {
        @Override
        public int [] getParameters() {
            return new int [] {};
        }
    }
    
    static abstract class AbstractCountGenerator extends StandardGenerator {
        final int count;
        
        AbstractCountGenerator(int count) {
            this.count = count;
            requirePositive(count);
        }
        AbstractCountGenerator(int [] parameters) {
            this.count = parameters[0];
        }
        
        @Override
        public int [] getParameters() {
            return new int [] { count };
        }
    }
    
    
    /**
     * Date range generator that generates a range from the reference date and having
     * a specified number of days.
     */
    public static class CurrentDate extends AbstractCountGenerator {
        /**
         * Constructor.
         * @param count The number of days for the range, going into the past.
         * This must be a positive integer.
         */
        public CurrentDate(int count) {
            super(count);
        }
        CurrentDate(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.CURRENT_DATE;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return new DateRange(refDate, refDate.minusDays(count - 1));
        }
    }
    
    
    /**
     * Retrieves a date range that is the calendar year that contains the reference date.
     * @param refDate   The reference date.
     * @param yearCount The number of years to include, must be positive.
     * @return The date range.
     */
    public static DateRange fromCurrentYear(LocalDate refDate, int yearCount) {
        requirePositive(yearCount);
        return new DateRange(DateUtil.getEndOfYear(refDate), DateUtil.getStartOfYear(refDate).minusYears(yearCount - 1));
    }
    
    /**
     * Generator that calls {@link #fromCurrentYear(java.time.LocalDate, int) }.
     */
    public static class CurrentYear extends AbstractCountGenerator {
        public CurrentYear(int yearCount) {
            super(yearCount);
        }
        CurrentYear(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.CURRENT_YEAR;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return fromCurrentYear(refDate, count);
        }
    }
    
    
    /**
     * Retrieves a date range that is the calendar month that contains the reference date.
     * @param refDate   The reference date.
     * @param monthCount    The number of months to include, must be positive.
     * @return The date range.
     */
    public static DateRange fromCurrentMonth(LocalDate refDate, int monthCount) {
        requirePositive(monthCount);
        return new DateRange(DateUtil.getEndOfMonth(refDate), DateUtil.getStartOfMonth(refDate).minusMonths(monthCount - 1));
    }
    
    /**
     * Generator that calls {@link #fromCurrentMonth(java.time.LocalDate, int) }.
     */
    public static class CurrentMonth extends AbstractCountGenerator {
        public CurrentMonth(int monthCount) {
            super(monthCount);
        }
        CurrentMonth(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.CURRENT_MONTH;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return fromCurrentMonth(refDate, count);
        }
    }
    
    
    /**
     * Retrieves a date range that is the calendar quarter that contains the reference date.
     * @param refDate   The reference date.
     * @param quarterCount  The number of quarters, must be a positive integer.
     * @return The date range.
     */
    public static DateRange fromCurrentQuarter(LocalDate refDate, int quarterCount) {
        requirePositive(quarterCount);
        return new DateRange(DateUtil.getEndOfQuarter(refDate), DateUtil.getStartOfQuarter(refDate).minusMonths((quarterCount - 1) * 3));
    }
    
    /**
     * Generator that calls {@link #fromCurrentQuarter(java.time.LocalDate, int) }.
     */
    public static class CurrentQuarter extends AbstractCountGenerator {
        public CurrentQuarter(int quarterCount) {
            super(quarterCount);
            requirePositive(count);
        }
        CurrentQuarter(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.CURRENT_QUARTER;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return fromCurrentQuarter(refDate, count);
        }
    }
    
    
    /**
     * Retrieves a date range that is the week that contains the reference date.
     * @param refDate   The reference date.
     * @param startOfWeek   The day of the week that starts the week. If <code>null</code>
     * {@link DayOfWeek#SUNDAY} is used.
     * @return The date range.
     */
    public static DateRange fromCurrentWeek(LocalDate refDate, DayOfWeek startOfWeek, int weekCount) {
        if (startOfWeek == null) {
            startOfWeek = DayOfWeek.SUNDAY;
        }
        LocalDate startDate = DateUtil.getClosestDayOfWeekOnOrBefore(refDate, startOfWeek);
        requirePositive(weekCount);
        return new DateRange(startDate.minusWeeks(weekCount - 1), startDate.plusDays(6));
    }
    
    /**
     * Generator that calls {@link #fromCurrentWeek(java.time.LocalDate, java.time.DayOfWeek, int) }.
     */
    public static class CurrentWeek extends StandardGenerator {
        private final DayOfWeek startOfWeek;
        private final int count;
        
        public CurrentWeek(DayOfWeek startOfWeek, int weekCount) {
            this.startOfWeek = startOfWeek;
            this.count = weekCount;
            requirePositive(count);
        }
        CurrentWeek(int [] parameters) {
            this((parameters[0] >= 1) ? DayOfWeek.of(parameters[0]) : null, parameters[1]);
        }

        @Override
        public Standard getStandard() {
            return Standard.CURRENT_WEEK;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return fromCurrentWeek(refDate, startOfWeek, count);
        }

        @Override
        public int[] getParameters() {
            return new int [] { (startOfWeek != null) ? startOfWeek.getValue() : -1, count };
        }
    }
    
    
    /**
     * Retrieves a date range that extends from the first day of the year of a reference
     * date up to and including the reference date.
     * @param refDate   The reference date.
     * @return The date range.
     */
    public static DateRange fromYearToDate(LocalDate refDate) {
        return new DateRange(refDate, DateUtil.getStartOfYear(refDate));
    }
    
    /**
     * Generator that calls {@link #fromYearToDate(java.time.LocalDate) }.
     */
    public static class YearToDate extends AbstractToDate {
        public YearToDate() {
        }
        YearToDate(int [] parameters) {
        }

        @Override
        public Standard getStandard() {
            return Standard.YEAR_TO_DATE;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return fromYearToDate(refDate);
        }
    }
    
    
    /**
     * Generator that calls {@link #fromMonthToDate(java.time.LocalDate) }.
     */
    public static class MonthToDate extends AbstractToDate {
        public MonthToDate() {
        }
        MonthToDate(int [] parameters) {
        }

        @Override
        public Standard getStandard() {
            return Standard.MONTH_TO_DATE;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return fromMonthToDate(refDate);
        }
    }

    /**
     * Retrieves a date range that extends from the first day of the month of a reference
     * date up to and including the reference date.
     * @param refDate   The reference date.
     * @return The date range.
     */
    public static DateRange fromMonthToDate(LocalDate refDate) {
        return new DateRange(refDate, DateUtil.getStartOfMonth(refDate));
    }
    
    /**
     * Generator that calls {@link #fromQuarterToDate(java.time.LocalDate) }.
     */
    public static class QuarterToDate extends AbstractToDate {
        public QuarterToDate() {
        }
        QuarterToDate(int [] parameters) {
        }

        @Override
        public Standard getStandard() {
            return Standard.QUARTER_TO_DATE;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return fromQuarterToDate(refDate);
        }
    }
    
    /**
     * Retrieves a date range that extends from the first day of the quarter of a reference
     * date up to and including the reference date.
     * @param refDate   The reference date.
     * @return The date range.
     */
    public static DateRange fromQuarterToDate(LocalDate refDate) {
        return new DateRange(refDate, DateUtil.getStartOfQuarter(refDate));
    }
    
    /**
     * Generator that calls {@link #lastYears(java.time.LocalDate, int) }.
     */
    public static class LastYears extends AbstractCountGenerator {
        public LastYears(int yearCount) {
            super(yearCount);
            requirePositive(this.count);
        }
        LastYears(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.LAST_YEARS;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return lastYears(refDate, count);
        }
    }
    
    /**
     * Retrieves a date range that consists of a specified number of 12 month periods
     * preceeding and including a reference date. The oldest date of the range will have
     * a day of the month that is one day after the day of the month of the reference date.
     * @param refDate   The reference date.
     * @param yearCount The number of 12 month periods. This must be a positive integer.
     * @return The date range.
     */
    public static DateRange lastYears(LocalDate refDate, int yearCount) {
        requirePositive(yearCount);
        return new DateRange(refDate, refDate.minusYears(yearCount).plusDays(1));
    }
    
    /**
     * Generator that calls {@link #lastMonths(java.time.LocalDate, int) }.
     */
    public static class LastMonths extends AbstractCountGenerator {
        public LastMonths(int monthCount) {
            super(monthCount);
            requirePositive(this.count);
        }
        LastMonths(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.LAST_MONTHS;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return lastMonths(refDate, count);
        }
    }
    
    /**
     * Retrieves a date range that consists of a specified number of months preceeding
     * and including a reference date. The oldest date of the range will have a day
     * of the month that is one day after the day of the month of the reference date.
     * @param refDate   The reference date.
     * @param monthCount    The number of months. This must be a positive integer.
     * @return The date range.
     */
    public static DateRange lastMonths(LocalDate refDate, int monthCount) {
        requirePositive(monthCount);
        return new DateRange(refDate, refDate.minusMonths(monthCount).plusDays(1));
    }
    
    /**
     * Generator that calls {@link #lastWeeks(java.time.LocalDate, int) }.
     */
    public static class LastWeeks extends AbstractCountGenerator {
        public LastWeeks(int weekCount) {
            super(weekCount);
            requirePositive(this.count);
        }
        LastWeeks(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.LAST_WEEKS;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return lastWeeks(refDate, count);
        }
    }
    
    /**
     * Retrieves a date range that consists of a specified number of weeks preceeding
     * and including a reference date. The oldest date of the range will have a day of
     * the week that is one day after the day of the week of the reference date.
     * @param refDate   The reference date.
     * @param weeksCount    The number of weeks. This must be a positive integer.
     * @return The date range.
     */
    public static DateRange lastWeeks(LocalDate refDate, int weeksCount) {
        requirePositive(weeksCount);
        return new DateRange(refDate, refDate.minusWeeks(weeksCount).plusDays(1));
    }
    
    
    /**
     * Generator that calls {@link #lastDays(java.time.LocalDate, int) }.
     */
    public static class LastDays extends AbstractCountGenerator {
        public LastDays(int dayCount) {
            super(dayCount);
            requirePositive(this.count);
        }
        LastDays(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.LAST_DAYS;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return lastDays(refDate, count);
        }
    }
    
    /**
     * Retrieves a date range that consists of a specified number of days preceeding
     * and including a reference date.
     * @param refDate   The reference date.
     * @param dayCount  The number of days in the range. This must be a positive integer.
     * @return The date range.
     */
    public static DateRange lastDays(LocalDate refDate, int dayCount) {
        requirePositive(dayCount);
        return new DateRange(refDate, refDate.minusDays(dayCount - 1));
    }
    
    
    /**
     * Generator that calls {@link #preceedingYears(java.time.LocalDate, int) }.
     */
    public static class PreceedingYears extends AbstractCountGenerator {
        public PreceedingYears(int yearCount) {
            super(yearCount);
            requirePositive(this.count);
        }
        PreceedingYears(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.PRECEEDING_YEARS;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return preceedingYears(refDate, count);
        }
    }
    
    /**
     * Retrieves a date range that consists of a number of calendar years immediately preceding
     * the year of a reference date.
     * <p>
     * For example:
     * <pre><code>
     *      DateRange range = DateRange.preceedingYears(LocalDate.of(2018, 2, 11), 3);
     *      range.getOldestDate().equals(LocalDate.of(2015, 1, 1));
     *      range.getNewestDate().equals(LocalDate.of(2017, 12, 31));
     * </code></pre>
     * @param refDate   The reference date.
     * @param yearCount The number of years to include. This must be a positive integer.
     * @return The date range.
     */
    public static DateRange preceedingYears(LocalDate refDate, int yearCount) {
        refDate = DateUtil.getEndOfYear(refDate).minusYears(1);
        return lastYears(refDate, yearCount);
    }
    

    /**
     * Generator that calls {@link #preceedingMonths(java.time.LocalDate, int) }.
     */
    public static class PreceedingMonths extends AbstractCountGenerator {
        public PreceedingMonths(int monthCount) {
            super(monthCount);
            requirePositive(this.count);
        }
        public PreceedingMonths(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.PRECEEDING_MONTHS;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return preceedingMonths(refDate, count);
        }
    }

    /**
     * Retrieves a date range that consists of a number of calendar months immediately preceding
     * the month of a reference date.
     * <p>
     * For example:
     * <pre><code>
     *      DateRange range = DateRange.preceedingMonths(LocalDate.of(2018, 2, 11), 3);
     *      range.getOldestDate().equals(LocalDate.of(2017, 11, 1));
     *      range.getNewestDate().equals(LocalDate.of(2018, 1, 31));
     * </code></pre>
     * @param refDate   The reference date.
     * @param monthCount    The number of months to include. This must be a positive integer.
     * @return The date range.
     */
    public static DateRange preceedingMonths(LocalDate refDate, int monthCount) {
        requirePositive(monthCount);
        refDate = DateUtil.getEndOfMonth(refDate.minusMonths(1));
        LocalDate oldestDate = DateUtil.getStartOfMonth(refDate.minusMonths(monthCount - 1));
        return new DateRange(refDate, oldestDate);
    }
    

    /**
     * Generator that calls {@link #preceedingQuarters(java.time.LocalDate, int) }.
     */
    public static class PreceedingQuarters extends AbstractCountGenerator {
        public PreceedingQuarters(int quarterCount) {
            super(quarterCount);
            requirePositive(this.count);
        }
        public PreceedingQuarters(int [] parameters) {
            this(parameters[0]);
        }

        @Override
        public Standard getStandard() {
            return Standard.PRECEEDING_QUARTERS;
        }

        @Override
        public DateRange generateRange(LocalDate refDate) {
            return preceedingQuarters(refDate, count);
        }
    }

    /**
     * Retrieves a date range that consists of a number of calendar quarters immediately
     * preceding the quarter of a reference date.
     * <p>
     * For example:
     * <pre><code>
     *      DateRange range = DateRange.preceedingQuarters(LocalDate.of(2018, 2, 11), 3);
     *      range.getOldestDate().equals(LocalDate.of(2017, 4, 1));
     *      range.getNewestDate().equals(LocalDate.of(2017, 12, 31));
     * </code></pre>
     * @param refDate   The reference date.
     * @param quarterCount  The number of months to include. This must be a positive integer.
     * @return The date range.
     */
    public static DateRange preceedingQuarters(LocalDate refDate, int quarterCount) {
        requirePositive(quarterCount);
        refDate = DateUtil.getEndOfQuarter(refDate).minusMonths(3);
        LocalDate oldestDate = DateUtil.getStartOfQuarter(refDate.minusMonths(quarterCount * 3 - 1));
        return new DateRange(refDate, oldestDate);
    }
    
}
