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

import com.leeboardtools.util.ArrayUtil;
import com.leeboardtools.util.ResourceSource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import javafx.util.StringConverter;

/**
 * Interface for defining a date offset, the offset is applied by the interface
 * to given arbitrary dates.
 * @author Albert Santos
 */
public interface DateOffset {
    
    /**
     * Applies the offset to a reference date.
     * @param refDate   The reference date.
     * @return The adjusted date.
     */
    public LocalDate getOffsetDate(LocalDate refDate);
    
    
    /**
     * Date offset that returns the reference date.
     */
    public static final DateOffset.Basic SAME_DAY = new Basic(Interval.DAY, 0, IntervalRelation.FIRST_DAY);
    
    /**
     * Date offset that returns the date that is one year prior to the reference date.
     */
    public static final DateOffset.Basic TWELVE_MONTHS_PRIOR = new Basic(Interval.YEAR, -1, IntervalRelation.CURRENT_DAY);
    
    /**
     * Date offset that returns the date that is one month prior to the reference date.
     */
    public static final DateOffset.Basic ONE_MONTH_PRIOR = new Basic(Interval.MONTH, -1, IntervalRelation.CURRENT_DAY);
    
    /**
     * Date offset that returns the first day of the reference date's year.
     */
    public static final DateOffset.Basic START_OF_YEAR = new Basic(Interval.YEAR, 0, IntervalRelation.FIRST_DAY);

    /**
     * Date offset that returns the last day of the reference date's year.
     */
    public static final DateOffset.Basic END_OF_YEAR = new Basic(Interval.YEAR, 0, IntervalRelation.LAST_DAY);

    /**
     * Date offset that returns the first day of the reference date's month.
     */
    public static final DateOffset.Basic START_OF_MONTH = new Basic(Interval.MONTH, 0, IntervalRelation.FIRST_DAY);

    /**
     * Date offset that returns the last day of the reference date's month.
     */
    public static final DateOffset.Basic END_OF_MONTH = new Basic(Interval.MONTH, 0, IntervalRelation.LAST_DAY);

    /**
     * Date offset that returns the last day of the year before the reference date's year.
     */
    public static final DateOffset.Basic END_OF_LAST_YEAR = new Basic(Interval.YEAR, 1, IntervalRelation.LAST_DAY);

    /**
     * Date offset that returns the first day of the year before the reference date's year.
     */
    public static final DateOffset.Basic START_OF_LAST_YEAR = new Basic(Interval.YEAR, -1, IntervalRelation.FIRST_DAY);

    /**
     * Date offset that returns the last day of the month before the reference date's month.
     */
    public static final DateOffset.Basic END_OF_LAST_MONTH = new Basic(Interval.MONTH, 1, IntervalRelation.LAST_DAY);

    /**
     * Date offset that returns the first day of the month before the reference date's month.
     */
    public static final DateOffset.Basic START_OF_LAST_MONTH = new Basic(Interval.MONTH, -1, IntervalRelation.FIRST_DAY);
    
    
    /**
     * The basic intervals.
     */
    public static enum Interval {
        DAY("LBTime.DateOffset.Interval.Day"),
        WEEK("LBTime.DateOffset.Interval.Week"),
        MONTH("LBTime.DateOffset.Interval.Month"),
        QUARTER("LBTime.DateOffset.Interval.Quarter"),
        YEAR("LBTime.DateOffset.Interval.Year");
        
        private final String stringResourceId;
        private Interval(String stringResourceId) {
            this.stringResourceId = stringResourceId;
        }
        public final String getStringResourceId() {
            return this.stringResourceId;
        }
        
        private static final Interval [] valuesNoDayArray = { WEEK, MONTH, QUARTER, YEAR };
        public static final Interval []  valuesNoDay() {
            return valuesNoDayArray;
        }
    }
    
    /**
     * String converter for {@link Interval}.
     */
    public static class IntervalStringConverter extends StringConverter<Interval> {
        private static String [] text;
        
        private static void loadText() {
            if (text == null) {
                text = new String [Interval.values().length];
                for (int i = 0; i < text.length; ++i) {
                    text[i] = ResourceSource.getString(Interval.values()[i].getStringResourceId());
                }
            }
        }
        
        @Override
        public String toString(DateOffset.Interval object) {
            loadText();
            if (object != null) {
                return text[object.ordinal()];
            }
            return null;
        }

        @Override
        public DateOffset.Interval fromString(String string) {
            loadText();
            for (int i =0; i < text.length; ++i) {
                if (text[i].equals(string)) {
                    return Interval.values()[i];
                }
            }
            return null;
        }
    }
    public static final IntervalStringConverter INTERVAL_STRING_CONVERTER = new IntervalStringConverter();
    
    
    /**
     * Determines which where in the interval to work from. Note that for {@link #LAST_DAY},
     * positive offsets go back in time.
     */
    enum IntervalRelation {
        /**
         * The interval offset is applied to the first day of the interval.
         */
        FIRST_DAY("LBTime.DateOffset.IntervalRelation.FirstDay"),
        
        /**
         * The interval offset is applied directly to the reference date.
         */
        CURRENT_DAY("LBTime.DateOffset.IntervalRelation.CurrentDay"),
        
        /**
         * The interval offset is applied to the last day of the interval.
         */
        LAST_DAY("LBTime.DateOffset.IntervalRelation.LastDay"),
        ;
        
        private String stringResourceId;
        private IntervalRelation(String stringResourceId) {
            this.stringResourceId = stringResourceId;
        }
        public final String getStringResourceId() {
            return this.stringResourceId;
        }
        
        
        private static IntervalRelation [] valuesNoCurrent = { FIRST_DAY, LAST_DAY };
        public static IntervalRelation [] valuesNoCurrentDay() {
            return valuesNoCurrent;
        }
    }
    
    public static class IntervalRelationStringConverter extends StringConverter<IntervalRelation> {
        private static String [] text;
        
        private static void loadText() {
            if (text == null) {
                text = new String [IntervalRelation.values().length];
                for (int i = 0; i < text.length; ++i) {
                    text[i] = ResourceSource.getString(IntervalRelation.values()[i].getStringResourceId());
                }
            }
        }

        @Override
        public String toString(IntervalRelation object) {
            loadText();
            if (object != null) {
                return text[object.ordinal()];
            }
            return null;
        }

        @Override
        public IntervalRelation fromString(String string) {
            loadText();
            for (int i =0; i < text.length; ++i) {
                if (text[i].equals(string)) {
                    return IntervalRelation.values()[i];
                }
            }
            return null;
        }
    }
    public static final IntervalRelationStringConverter INTERVAL_RELATION_STRING_CONVERTER = new IntervalRelationStringConverter();
    
    
    /**
     * Interface for the date offset to apply after the reference date has been
     * adjusted to the basic interval.
     */
    public interface SubIntervalOffset extends DateOffset {
        /**
         * Applies the offset to the reference date in the reverse direction.
         * @param refDate   The reference date.
         * @return The offset date.
         */
        public LocalDate getReverseOffsetDate(LocalDate refDate);
    }
    
    /**
     * Sub-interval offset that simply applies a number of days.
     */
    public static class DayOffset implements SubIntervalOffset {
        private final int dayCount;
        
        /**
         * Constructor.
         * @param dayCount The number of days to add.
         */
        public DayOffset(int dayCount) {
            this.dayCount = dayCount;
        }
        
        /**
         * @return The number of days to add.
         */
        public final int getDayCount() {
            return dayCount;
        }

        @Override
        public LocalDate getOffsetDate(LocalDate refDate) {
            return refDate.plusDays(dayCount);
        }
        
        @Override
        public LocalDate getReverseOffsetDate(LocalDate refDate) {
            return refDate.minusDays(dayCount);
        }
    }
    
    /**
     * Sub-interval offset that selects the nth occurrence of a particular day of week
     * on or after the reference date.
     * Some examples (LocalDate.of(2018, 2, 12) is a Monday)
     * <pre><code>
     *      // This returns the second Monday from the reference date, inclusive of the reference date.
     *      DateOffset offset = new DateOffset.NthDayOfWeekOffset(DayOfWeek.MONDAY, 2);
     *      LocalDate date = offset.getOffsetDate(LocalDate.of(2018, 2, 12));
     *      assertEquals(LocalDate.of(2018, 2, 19), date);
     * 
     *      // This returns the 3rd Sunday from the reference date.
     *      offset = new DateOffset.NthDayOfWeekOffset(DayOfWeek.SUNDAY, 3);
     *      date = offset.getOffsetDate(LocalDate.of(2018, 2, 12));
     *      assertEquals(LocalDate.of(2018, 3, 4), date);
     * 
     *      // This returns the 1st Tuesday from the reference date.
     *      offset = new DateOffset.NthDayOfWeekOffset(DayOfWeek.TUESDAY, 1);
     *      date = offset.getOffsetDate(LocalDate.of(2018, 2, 12));
     *      assertEquals(LocalDate.of(2018, 2, 13), date);
     * </code></pre>
     */
    public static class NthDayOfWeekOffset implements SubIntervalOffset {
        private final DayOfWeek dayOfWeek;
        private final int occurrence;
        
        /**
         * Constructor.
         * @param dayOfWeek The day of week of interest.
         * @param occurrence The number of occurrences, this should normally be a non-zero
         * integer.
         */
        public NthDayOfWeekOffset(DayOfWeek dayOfWeek, int occurrence) {
            this.dayOfWeek = dayOfWeek;
            this.occurrence = occurrence;
        }
        
        /**
         * @return The day of week of the offset date returned by {@link #getOffsetDate(java.time.LocalDate) }.
         */
        public final DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }
        
        /**
         * @return The number of occurrences of the day of the week from the reference date, 
         * if the reference date falls on the day of the week an occurrence of 1 will
         * return the reference date.
         */
        public final int getOccurrence() {
            return occurrence;
        }

        @Override
        public LocalDate getOffsetDate(LocalDate refDate) {
            LocalDate date = DateUtil.getClosestDayOfWeekOnOrAfter(refDate, dayOfWeek);
            return date.plusWeeks(occurrence - 1);
        }

        @Override
        public LocalDate getReverseOffsetDate(LocalDate refDate) {
            LocalDate date = DateUtil.getClosestDayOfWeekOnOrBefore(refDate, dayOfWeek);
            return date.minusWeeks(occurrence - 1);
        }
    }

    
    /**
     * A {@link DateOffset} implementation that provides an offset over an {@link Interval}
     * and then an optional offset within the interval using a {@link SubIntervalOffset}.
     */
    public static class Basic implements DateOffset {
        private final Interval interval;
        private final int intervalOffset;
        private final IntervalRelation intervalRelation;
        private final SubIntervalOffset subIntervalOffset;
        private final DayOfWeek startOfWeek;
        
        /**
         * Constructor.
         * @param interval  The offset interval.
         * @param intervalOffset    The number of intervals to offset by.
         * @param intervalRelation   The reference point of the interval by which to offset.
         *  Note that {@link IntervalRelation#LAST_DAY} reverses the offset directions, that is,
         *  positive offsets are in the past.
         * @param subIntervalOffset The optional sub-interval to add to the interval offset date.
         * If <code>null</code> no sub-interval offset is applied.
         * @param startOfWeek For use when interval is {@link Interval#WEEK} to specify the
         * day that starts the week. If <code>null</code> then the day of the week returned
         * by {@link DateUtil#getDefaultFirstDayOfWeek() } will be used.
         */
        public Basic(Interval interval, int intervalOffset, IntervalRelation intervalRelation, 
                SubIntervalOffset subIntervalOffset, DayOfWeek startOfWeek) {
            this.interval = interval;
            this.intervalOffset = intervalOffset;
            this.intervalRelation = intervalRelation;
            this.subIntervalOffset = subIntervalOffset;
            this.startOfWeek = startOfWeek;
        }

        
        /**
         * Constructor.
         * @param interval  The offset interval.
         * @param intervalOffset    The number of intervals to offset by.
         * @param intervalRelation   The reference point of the interval by which to offset.
         *  Note that {@link IntervalRelation#LAST_DAY} reverses the offset directions, that is,
         *  positive offsets are in the past.
         */
        public Basic(Interval interval, int intervalOffset, IntervalRelation intervalRelation) {
            this(interval, intervalOffset, intervalRelation, null, null);
        }

        
        /**
         * Constructor.
         * @param interval  The offset interval.
         * @param intervalOffset    The number of intervals to offset by.
         * @param intervalRelation   The reference point of the interval by which to offset.
         *  Note that {@link IntervalRelation#LAST_DAY} reverses the offset directions, that is,
         *  positive offsets are in the past.
         * @param startOfWeek For use when interval is {@link Interval#WEEK} to specify the
         * day that starts the week. If <code>null</code> then the day of the week returned
         * by {@link DateUtil#getDefaultFirstDayOfWeek() } will be used.
         */
        public Basic(Interval interval, int intervalOffset, IntervalRelation intervalRelation, DayOfWeek startOfWeek) {
            this(interval, intervalOffset, intervalRelation, null, startOfWeek);
        }
        
        /**
         * @return The offset interval.
         */
        public final Interval getInterval() {
            return interval;
        }
        
        /**
         * @return The number of intervals to offset by. The direction in time is determined
         * by {@link #getIntervalRelation() }.
         */
        public final int getIntervalOffset() {
            return intervalOffset;
        }
        
        /**
         * @return The reference point of the interval by which to offset.
         *  Note that {@link IntervalRelation#LAST_DAY} reverses the offset directions, that is,
         *  positive offsets are in the past.
         */
        public final IntervalRelation getIntervalRelation() {
            return intervalRelation;
        }
        
        /**
         * @return The optional sub-interval to add to the interval offset date, <code>null</code>
         * if no sub-interval is to be applied.
         */
        public final SubIntervalOffset getSubIntervalOffset() {
            return subIntervalOffset;
        }
        
        /**
         * @return Used when the interval is {@link Interval#WEEK} to specify the
         * day that starts the week. If <code>null</code> then the day of the week returned
         * by {@link DateUtil#getDefaultFirstDayOfWeek() } will be used.
         */
        public final DayOfWeek getStartOfWeek() {
            return startOfWeek;
        }
        
        
        /**
         * Retrieves a new {@link Basic} date offset that is identical to this date offset except
         * that an amount has been added to the interval offset.
         * @param delta The amount to add to the interval offset.
         * @return The new date offset.
         */
        public Basic plusIntervalOffset(int delta) {
            return new Basic(interval, intervalOffset + delta, intervalRelation, subIntervalOffset, startOfWeek);
        }
        
        
        @Override
        public LocalDate getOffsetDate(LocalDate refDate) {
            LocalDate offsetDate = refDate;
            switch (intervalRelation) {
                case FIRST_DAY :
                    switch (interval) {
                        case YEAR :
                            offsetDate = DateUtil.getStartOfYear(refDate).plusYears(intervalOffset);
                            break;
                            
                        case QUARTER :
                            offsetDate = DateUtil.getStartOfQuarter(refDate);
                            offsetDate = DateUtil.plusQuarters(offsetDate, intervalOffset);
                            break;
                            
                        case MONTH :
                            offsetDate = DateUtil.getStartOfMonth(refDate).plusMonths(intervalOffset);
                            break;
                            
                        case WEEK :
                            offsetDate = DateUtil.getClosestDayOfWeekOnOrBefore(refDate, DateUtil.getValidFirstDayOfWeek(startOfWeek))
                                    .plusWeeks(intervalOffset);
                            break;
                            
                        case DAY :
                            offsetDate = refDate.plusDays(intervalOffset);
                            break;
                    }
                    if (subIntervalOffset != null) {
                        offsetDate = subIntervalOffset.getOffsetDate(offsetDate);
                    }
                    break;
                    
                case CURRENT_DAY :
                    switch (interval) {
                        case YEAR :
                            offsetDate = refDate.plusYears(intervalOffset);
                            break;
                            
                        case QUARTER :
                            offsetDate = DateUtil.plusQuarters(refDate, intervalOffset);
                            break;
                            
                        case MONTH :
                            offsetDate = refDate.plusMonths(intervalOffset);
                            break;
                            
                        case WEEK :
                            offsetDate = refDate.plusWeeks(intervalOffset);
                            break;
                            
                        case DAY :
                            offsetDate = refDate.plusDays(intervalOffset);
                            break;
                    }
                    if (subIntervalOffset != null) {
                        offsetDate = subIntervalOffset.getOffsetDate(offsetDate);
                    }
                    break;
                    
                case LAST_DAY :
                    // We have to offset by the intervals before getting the end of the interval
                    // to make sure we get the actual end of the interval.
                    switch (interval) {
                        case YEAR :
                            offsetDate = refDate.minusYears(intervalOffset);
                            offsetDate = DateUtil.getEndOfYear(offsetDate);
                            break;
                            
                        case QUARTER :
                            offsetDate = DateUtil.minusQuarters(refDate, intervalOffset);
                            offsetDate = DateUtil.getEndOfQuarter(offsetDate);
                            break;

                        case MONTH :
                            offsetDate = refDate.minusMonths(intervalOffset);
                            offsetDate = DateUtil.getEndOfMonth(offsetDate);
                            break;
                            
                        case WEEK :
                            offsetDate = DateUtil.getClosestDayOfWeekOnOrAfter(refDate, DateUtil.getValidFirstDayOfWeek(startOfWeek))
                                    .minusWeeks(intervalOffset);
                            break;
                            
                        case DAY :
                            offsetDate = refDate.minusDays(intervalOffset);
                            break;
                    }
                    if (subIntervalOffset != null) {
                        offsetDate = subIntervalOffset.getReverseOffsetDate(offsetDate);
                    }
                    break;
            }
            
            
            return offsetDate;
        }
    
    }
    
    
    
}
