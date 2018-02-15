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
     * The standard date offsets supported here.
     */
    public static enum Standard {
        NULL,
        DAYS_OFFSET,
        NTH_DAY_OF_WEEK,
        WEEKS_OFFSET,
        MONTHS_OFFSET,
        QUARTERS_OFFSET,
        YEARS_OFFSET,
        ;
    }
    
    
    /**
     * Interface implemented by the standard date offsets.
     */
    public interface StandardDateOffset extends DateOffset {
        
        /**
         * @return The standard this implements.
         */
        public Standard getStandard();

        /**
         * @return The parameters for the standard.
         */
        public int [] getParameters();
    }
    
    
    
    /**
     * Creates a date offset for a given standard and its parameters.
     * @param standard  The standard to create.
     * @param parameters    The parameters for the standard.
     * @param baseIndex The index into parameters of the first parameter to use.
     * @return The standard date offset.
     */
    public static StandardDateOffset fromStandard(Standard standard, int [] parameters, int baseIndex) {
        switch(standard) {
            case NULL :
                return new Null();
                
            case DAYS_OFFSET :
                return new DaysOffset(parameters, baseIndex);
                
            case NTH_DAY_OF_WEEK :
                return new NthDayOfWeek(parameters, baseIndex);
                
            case WEEKS_OFFSET :
                return new WeeksOffset(parameters, baseIndex);
                
            case MONTHS_OFFSET :
                return new MonthsOffset(parameters, baseIndex);
                
            case QUARTERS_OFFSET :
                return new QuartersOffset(parameters, baseIndex);
                
            case YEARS_OFFSET :
                return new YearsOffset(parameters, baseIndex);
        }
        throw new IllegalArgumentException("standard argument is invalid.");
    }
    
    /**
     * Helper that applies a date offset if it is not <code>null</code>.
     * @param dateOffset    The date offset to be applied.
     * @param refDate   The reference date.
     * @return The adjusted date.
     */
    public static LocalDate applyDateOffset(DateOffset dateOffset, LocalDate refDate) {
        return (dateOffset == null) ? refDate : dateOffset.getOffsetDate(refDate);
    }
    
    
    /**
     * A date offset that simply returns the reference date.
     */
    public static class Null implements StandardDateOffset {
        public Null() {
        }

        @Override
        public Standard getStandard() {
            return Standard.NULL;
        }

        @Override
        public LocalDate getOffsetDate(LocalDate refDate) {
            return refDate;
        }

        @Override
        public int[] getParameters() {
            return new int [0];
        }
    }
    

    /**
     * A date offset that adds a number of days to the reference date.
     */
    public static class DaysOffset implements StandardDateOffset {
        final int count;
        
        public DaysOffset(int dayCount) {
            this.count = dayCount;
        }
        DaysOffset(int [] parameters, int baseIndex) {
            this(parameters[baseIndex]);
        }

        @Override
        public Standard getStandard() {
            return Standard.DAYS_OFFSET;
        }

        @Override
        public LocalDate getOffsetDate(LocalDate refDate) {
            return refDate.plusDays(count);
        }

        @Override
        public int[] getParameters() {
            return new int [] { count };
        }
    }
    
    
    /**
     * Used by date offsets that work with a date range to define whether the days offset
     * is relative to the first day of the range or the last day of the range.
     * When it is relative to the last day of the range, positive day values are in the
     * past relative to the last day, negative day values are in the future.
     */
    enum OffsetReference {
        FIRST_DAY,
        LAST_DAY,
        ;
    }
    
    static abstract class AbstractStandardDateOffset implements StandardDateOffset {
        final int baseOffset;
        final int daysOffset;
        final OffsetReference offsetReference;
        final StandardDateOffset subDateOffset;
        
        AbstractStandardDateOffset(int baseOffset, int daysOffset, OffsetReference offsetReference, StandardDateOffset subDateOffset) {
            this.baseOffset = baseOffset;
            this.daysOffset = daysOffset;
            this.offsetReference = offsetReference;
            this.subDateOffset = subDateOffset;
        }
        
        AbstractStandardDateOffset(int [] parameters, int baseIndex) {
            this.baseOffset = parameters[baseIndex + 0];
            this.daysOffset = parameters[baseIndex + 1];
            this.offsetReference = OffsetReference.values()[parameters[baseIndex + 2]];
            if (parameters.length > baseIndex + 3) {
                int standardIndex = parameters[baseIndex + 3];
                Standard standard = Standard.values()[standardIndex];
                this.subDateOffset = fromStandard(standard, parameters, baseIndex + 4);
            }
            else {
                this.subDateOffset = null;
            }
        }

        @Override
        public int[] getParameters() {
            int [] myParameters = getMyParameters();
            if (this.subDateOffset == null) {
                return myParameters;
            }
            
            int [] subParameters = this.subDateOffset.getParameters();
            return ArrayUtil.join(myParameters, new int [] { this.subDateOffset.getStandard().ordinal() }, subParameters);
        }

        @Override
        public LocalDate getOffsetDate(LocalDate refDate) {
            LocalDate date;
            switch (offsetReference) {
                case FIRST_DAY :
                    date = getFirstDayOffsetDate(refDate).plusDays(daysOffset);
                    break;
                case LAST_DAY :
                    date = getLastDayOffsetDate(refDate).minusDays(daysOffset);
                    break;
                default :
                    throw new IllegalStateException("offsetReference is invalid");
            }
            
            if (subDateOffset != null) {
                date = subDateOffset.getOffsetDate(date);
            }
            return date;
        }
        
        
        int [] getMyParameters() {
            return new int [] { baseOffset, daysOffset, offsetReference.ordinal() };
        }

        abstract LocalDate getFirstDayOffsetDate(LocalDate refDate);
        abstract LocalDate getLastDayOffsetDate(LocalDate refDate);
    }
    
    
    /**
     * A date offset that uses a week as the range, with a  {@link DayOfWeek} argument
     * defining the first day of the week.
     */
    public static class WeeksOffset extends AbstractStandardDateOffset {
        final DayOfWeek startOfWeek;
        
        /**
         * Constructor.
         * @param startOfWeek   The day of the week defining the first day of the week.
         * @param weeksOffset   The number of weeks to offset the final week. If offsetReference
         * is {@link OffsetReference#FIRST_DAY} then positive values move the week into the
         * future, otherwise positive values move the week into the past.
         * @param daysOffset    The day offset to add to the offset reference date calculated
         * for the weeksOffset, the sign direction matches the sign direction of weeksOffset.
         * @param offsetReference The offset reference, if {@link OffsetReference#FIRST_DAY}
         * then a 0 daysOffset will return a date whose day of the week is startOfWeek.
         * If {@link OffsetReference#LAST_DAY} the a 0 daysOffset will return a date whose
         * day of the week is the day of the week before startOfWeek.
         */
        public WeeksOffset(DayOfWeek startOfWeek, int weeksOffset, int daysOffset, OffsetReference offsetReference) {
            super(weeksOffset, daysOffset, offsetReference, null);
            this.startOfWeek = startOfWeek;
        }
        WeeksOffset(int [] parameters, int baseIndex) {
            super(parameters, baseIndex + 1);
            this.startOfWeek = DayOfWeek.of(parameters[0]);
        }

        @Override
        int[] getMyParameters() {
            int [] superParameters = super.getMyParameters();
            return ArrayUtil.join(new int [] { startOfWeek.getValue() }, superParameters);
        }

        @Override
        LocalDate getFirstDayOffsetDate(LocalDate refDate) {
            return DateUtil.getClosestDayOfWeekOnOrBefore(refDate, startOfWeek).plusWeeks(baseOffset);
        }

        @Override
        LocalDate getLastDayOffsetDate(LocalDate refDate) {
            return DateUtil.getClosestDayOfWeekOnOrAfter(refDate, startOfWeek).minusDays(1).minusWeeks(baseOffset);
        }

        @Override
        public Standard getStandard() {
            return Standard.WEEKS_OFFSET;
        }

    }
    
    
    /**
     * A date offset that returns a particular occurrence of a day of the week relative
     * to the reference date. The occurrences are inclusive.
     * <p>
     * Some examples (LocalDate.of(2018, 2, 12) is a Monday)
     * <pre><code>
     *      // This returns the second Monday from the reference date, inclusive of the reference date.
     *      DateOffset offset = new DateOffset.NthDayOfWeek(DayOfWeek.MONDAY, 2);
     *      LocalDate date = offset.getOffsetDate(LocalDate.of(2018, 2, 12));
     *      assertEquals(LocalDate.of(2018, 2, 19), date);
     * 
     *      // This returns the 3rd Sunday from the reference date.
     *      offset = new DateOffset.NthDayOfWeek(DayOfWeek.SUNDAY, 3);
     *      date = offset.getOffsetDate(LocalDate.of(2018, 2, 12));
     *      assertEquals(LocalDate.of(2018, 3, 4), date);
     * 
     *      // This returns the 1st Tuesday from the reference date.
     *      offset = new DateOffset.NthDayOfWeek(DayOfWeek.TUESDAY, 1);
     *      date = offset.getOffsetDate(LocalDate.of(2018, 2, 12));
     *      assertEquals(LocalDate.of(2018, 2, 13), date);
     * </code></pre>
     */
    public static class NthDayOfWeek implements StandardDateOffset {
        final DayOfWeek dayOfWeek;
        final int occurrence;
        
        /**
         * Constructor.
         * @param dayOfWeek The day of the week of interest.
         * @param occurrence The nth occurrence of the of the day of the week from
         * the reference date to return. This should normally be a positive integer.
         */
        public NthDayOfWeek(DayOfWeek dayOfWeek, int occurrence) {
            this.dayOfWeek = dayOfWeek;
            this.occurrence = occurrence;
        }
        
        NthDayOfWeek(int [] parameters, int baseIndex) {
            this(DayOfWeek.of(parameters[baseIndex]), parameters[baseIndex + 1]);
        }

        @Override
        public Standard getStandard() {
            return Standard.NTH_DAY_OF_WEEK;
        }

        @Override
        public int[] getParameters() {
            return new int [] { dayOfWeek.getValue(), occurrence };
        }

        @Override
        public LocalDate getOffsetDate(LocalDate refDate) {
            LocalDate date = DateUtil.getClosestDayOfWeekOnOrAfter(refDate, dayOfWeek);
            return date.plusWeeks(occurrence - 1);
        }
    }
    
    
    /**
     * A date offset that uses calendar months as the basic unit.
     */
    public static class MonthsOffset extends AbstractStandardDateOffset {
        
        /**
         * Constructor.
         * @param monthsOffset  The number of months to offset from the month of the
         * reference date. If offsetReference is {@link OffsetReference#FIRST_DAY} then positive
         * values advance the month into the future, otherwise positive values advance the
         * month into the past.
         * @param daysOffset    The number of days to add to the offset date calculated for
         * the months offset. The sign direction matches that of monthsOffset.
         * @param offsetReference   The offset reference, if {@link OffsetReference#FIRST_DAY}
         * then a 0 daysOffset will return a date that is the first of the month, otherwise a 
         * 0 daysOffset will return a date that is the last day of the month.
         * @param subDateOffset An optional date offset to apply to the calculated offset date.
         */
        public MonthsOffset(int monthsOffset, int daysOffset, OffsetReference offsetReference, StandardDateOffset subDateOffset) {
            super(monthsOffset, daysOffset, offsetReference, subDateOffset);
        }
        MonthsOffset(int [] parameters, int baseIndex) {
            super(parameters, baseIndex);
        }

        @Override
        LocalDate getFirstDayOffsetDate(LocalDate refDate) {
            return DateUtil.getStartOfMonth(refDate).plusMonths(baseOffset);
        }

        @Override
        LocalDate getLastDayOffsetDate(LocalDate refDate) {
            // We need to adjust by the base offset before jumping to the end so we don't
            // lose dates due to differing last day of the month.
            return DateUtil.getEndOfMonth(refDate.minusMonths(baseOffset));
        }

        @Override
        public Standard getStandard() {
            return Standard.MONTHS_OFFSET;
        }

    }
    
    
    /**
     * A date offset that uses calendar quarters as the basic unit.
     */
    public static class QuartersOffset extends AbstractStandardDateOffset {
        
        
        /**
         * Constructor.
         * @param quartersOffset  The number of quarters to offset from the quarter of the
         * reference date. If offsetReference is {@link OffsetReference#FIRST_DAY} then positive
         * values advance the quarter into the future, otherwise positive values advance the
         * quarter into the past.
         * @param daysOffset    The number of days to add to the offset date calculated for
         * the quarters offset. The sign direction matches that of quartersOffset.
         * @param offsetReference   The offset reference, if {@link OffsetReference#FIRST_DAY}
         * then a 0 daysOffset will return a date that is the first of the quarter, otherwise a 
         * 0 daysOffset will return a date that is the last day of the quarter.
         * @param subDateOffset An optional date offset to apply to the calculated offset date.
         */
        public QuartersOffset(int quartersOffset, int daysOffset, OffsetReference offsetReference, StandardDateOffset subDateOffset) {
            super(quartersOffset, daysOffset, offsetReference, subDateOffset);
        }
        QuartersOffset(int [] parameters, int baseIndex) {
            super(parameters, baseIndex);
        }

        @Override
        LocalDate getFirstDayOffsetDate(LocalDate refDate) {
            return DateUtil.getStartOfQuarter(refDate).plusMonths(baseOffset * 3);
        }

        @Override
        LocalDate getLastDayOffsetDate(LocalDate refDate) {
            // We need to adjust by the base offset before jumping to the end so we don't
            // lose dates due to differing last day of the month.
            return DateUtil.getEndOfQuarter(refDate.minusMonths(baseOffset * 3));
        }

        @Override
        public Standard getStandard() {
            return Standard.QUARTERS_OFFSET;
        }

    }
    
    
    /**
     * A date offset that uses calendar years as the basic unit.
     */
    public static class YearsOffset extends AbstractStandardDateOffset {
        
        
        /**
         * Constructor.
         * @param yearsOffset  The number of years to offset from the year of the
         * reference date. If offsetReference is {@link OffsetReference#FIRST_DAY} then positive
         * values advance the year into the future, otherwise positive values advance the
         * year into the past.
         * @param daysOffset    The number of days to add to the offset date calculated for
         * the years offset. The sign direction matches that of yearsOffset.
         * @param offsetReference   The offset reference, if {@link OffsetReference#FIRST_DAY}
         * then a 0 daysOffset will return a date that is the first of the calendar year, otherwise a 
         * 0 daysOffset will return a date that is the last day of the calendar year.
         * @param subDateOffset An optional date offset to apply to the calculated offset date.
         */
        public YearsOffset(int yearsOffset, int daysOffset, OffsetReference offsetReference, StandardDateOffset subDateOffset) {
            super(yearsOffset, daysOffset, offsetReference, subDateOffset);
        }
        YearsOffset(int [] parameters, int baseIndex) {
            super(parameters, baseIndex);
        }

        @Override
        LocalDate getFirstDayOffsetDate(LocalDate refDate) {
            return DateUtil.getStartOfYear(refDate).plusYears(baseOffset);
        }

        @Override
        LocalDate getLastDayOffsetDate(LocalDate refDate) {
            // We need to adjust by the base offset before jumping to the end so we don't
            // lose dates due to differing last day of the month.
            return DateUtil.getEndOfYear(refDate.minusYears(baseOffset));
        }

        @Override
        public Standard getStandard() {
            return Standard.YEARS_OFFSET;
        }

    }
    
}
