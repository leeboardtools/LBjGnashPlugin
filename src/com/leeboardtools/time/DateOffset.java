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
import java.time.DayOfWeek;
import java.time.LocalDate;

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
     * The basic intervals.
     */
    public static enum Interval {
        DAY,
        WEEK,
        MONTH,
        QUARTER,
        YEAR
    }
    
    
    /**
     * Determines which end of the interval to work from. Note that for {@link #LAST_DAY},
     * positive offsets go back in time.
     */
    enum IntervalEnd {
        FIRST_DAY,
        LAST_DAY,
        ;
    }
    
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
        private final IntervalEnd intervalEnd;
        private final SubIntervalOffset subIntervalOffset;
        private final DayOfWeek startOfWeek;
        
        /**
         * Constructor.
         * @param interval  The offset interval.
         * @param intervalOffset    The number of intervals to offset by.
         * @param intervalEnd   The reference point of the interval by which to offset.
         *  Note that {@link IntervalEnd#LAST_DAY} reverses the offset directions, that is,
         *  positive offsets are in the past.
         * @param subIntervalOffset The optional sub-interval to add to the interval offset date.
         * If <code>null</code> no sub-interval offset is applied.
         * @param startOfWeek For use when interval is {@link Interval#WEEK} to specify the
         * day that starts the week. If <code>null</code> then the day of the week returned
         * by {@link DateUtil#getDefaultFirstDayOfWeek() } will be used.
         */
        public Basic(Interval interval, int intervalOffset, IntervalEnd intervalEnd, 
                SubIntervalOffset subIntervalOffset, DayOfWeek startOfWeek) {
            this.interval = interval;
            this.intervalOffset = intervalOffset;
            this.intervalEnd = intervalEnd;
            this.subIntervalOffset = subIntervalOffset;
            this.startOfWeek = startOfWeek;
        }

        
        /**
         * Constructor.
         * @param interval  The offset interval.
         * @param intervalOffset    The number of intervals to offset by.
         * @param intervalEnd   The reference point of the interval by which to offset.
         *  Note that {@link IntervalEnd#LAST_DAY} reverses the offset directions, that is,
         *  positive offsets are in the past.
         */
        public Basic(Interval interval, int intervalOffset, IntervalEnd intervalEnd) {
            this(interval, intervalOffset, intervalEnd, null, null);
        }

        
        /**
         * Constructor.
         * @param interval  The offset interval.
         * @param intervalOffset    The number of intervals to offset by.
         * @param intervalEnd   The reference point of the interval by which to offset.
         *  Note that {@link IntervalEnd#LAST_DAY} reverses the offset directions, that is,
         *  positive offsets are in the past.
         * @param startOfWeek For use when interval is {@link Interval#WEEK} to specify the
         * day that starts the week. If <code>null</code> then the day of the week returned
         * by {@link DateUtil#getDefaultFirstDayOfWeek() } will be used.
         */
        public Basic(Interval interval, int intervalOffset, IntervalEnd intervalEnd, DayOfWeek startOfWeek) {
            this(interval, intervalOffset, intervalEnd, null, startOfWeek);
        }
        
        /**
         * @return The offset interval.
         */
        public final Interval getInterval() {
            return interval;
        }
        
        /**
         * @return The number of intervals to offset by. The direction in time is determined
         * by {@link #getIntervalEnd() }.
         */
        public final int getIntervalOffset() {
            return intervalOffset;
        }
        
        /**
         * @return The reference point of the interval by which to offset.
         *  Note that {@link IntervalEnd#LAST_DAY} reverses the offset directions, that is,
         *  positive offsets are in the past.
         */
        public final IntervalEnd getIntervalEnd() {
            return intervalEnd;
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
        
        @Override
        public LocalDate getOffsetDate(LocalDate refDate) {
            LocalDate offsetDate = refDate;
            switch (intervalEnd) {
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
