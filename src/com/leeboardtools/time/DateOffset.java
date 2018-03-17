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

import com.leeboardtools.util.EnumStringConverter;
import com.leeboardtools.json.InvalidContentException;
import com.leeboardtools.json.JSONLite;
import com.leeboardtools.json.JSONObject;
import com.leeboardtools.json.JSONValue;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

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
    public static final DateOffset.Basic END_OF_LAST_YEAR = new Basic(Interval.YEAR, -1, IntervalRelation.LAST_DAY);

    /**
     * Date offset that returns the first day of the year before the reference date's year.
     */
    public static final DateOffset.Basic START_OF_LAST_YEAR = new Basic(Interval.YEAR, 1, IntervalRelation.FIRST_DAY);

    /**
     * Date offset that returns the last day of the month before the reference date's month.
     */
    public static final DateOffset.Basic END_OF_LAST_MONTH = new Basic(Interval.MONTH, -1, IntervalRelation.LAST_DAY);

    /**
     * Date offset that returns the first day of the month before the reference date's month.
     */
    public static final DateOffset.Basic START_OF_LAST_MONTH = new Basic(Interval.MONTH, 1, IntervalRelation.FIRST_DAY);
    
    
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
    public static final EnumStringConverter<Interval> INTERVAL_STRING_CONVERTER = new EnumStringConverter<Interval>() {
        @Override
        protected Interval[] getEnumValues() {
            return Interval.values();
        }

        @Override
        protected String getEnumStringResourceId(Interval enumValue) {
            return enumValue.getStringResourceId();
        }
    };
    

    /**
     * Determines which where in the interval to work from. Note that for {@link #LAST_DAY},
     * positive offsets go back in time.
     */
    public static enum IntervalRelation {
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
    
    public static final EnumStringConverter<IntervalRelation> INTERVAL_RELATION_STRING_CONVERTER = new EnumStringConverter<IntervalRelation> () {
        @Override
        protected IntervalRelation[] getEnumValues() {
            return IntervalRelation.values();
        }

        @Override
        protected String getEnumStringResourceId(IntervalRelation enumValue) {
            return enumValue.getStringResourceId();
        }
    };
    
    
    
    
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

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + this.dayCount;
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
            final DayOffset other = (DayOffset) obj;
            if (this.dayCount != other.dayCount) {
                return false;
            }
            return true;
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

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.dayOfWeek);
            hash = 53 * hash + this.occurrence;
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
            final NthDayOfWeekOffset other = (NthDayOfWeekOffset) obj;
            if (this.occurrence != other.occurrence) {
                return false;
            }
            if (this.dayOfWeek != other.dayOfWeek) {
                return false;
            }
            return true;
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

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + Objects.hashCode(this.interval);
            hash = 17 * hash + this.intervalOffset;
            hash = 17 * hash + Objects.hashCode(this.intervalRelation);
            hash = 17 * hash + Objects.hashCode(this.subIntervalOffset);
            hash = 17 * hash + Objects.hashCode(this.startOfWeek);
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
            final Basic other = (Basic) obj;
            if (this.intervalOffset != other.intervalOffset) {
                return false;
            }
            if (this.interval != other.interval) {
                return false;
            }
            if (this.intervalRelation != other.intervalRelation) {
                return false;
            }
            if (!Objects.equals(this.subIntervalOffset, other.subIntervalOffset)) {
                return false;
            }
            if (this.startOfWeek != other.startOfWeek) {
                return false;
            }
            return true;
        }
    
    }
    
    
    /**
     * Creates a {@link JSONObject} representing a {@link SubIntervalOffset} object.
     * @param offset    The offset object, may be <code>null</code>
     * @return The JSON object, <code>null</code> if offset is <code>null</code>
     */
    public static JSONObject toJSONObject(SubIntervalOffset offset) {
        if (offset == null) {
            return null;
        }

        JSONObject object = JSONLite.newJSONObject();
        object.putClassName(offset.getClass());

        if (offset instanceof DayOffset) {
            DayOffset dayOffset = (DayOffset)offset;
            object.add("dayCount", dayOffset.getDayCount());

        }
        else if (offset instanceof NthDayOfWeekOffset) {
            NthDayOfWeekOffset dayOfWeekOffset = (NthDayOfWeekOffset)offset;
            object.add("dayOfWeek", dayOfWeekOffset.getDayOfWeek());
            object.add("occurrence", dayOfWeekOffset.getOccurrence());

        }
        else {
            throw new UnsupportedOperationException("The SubIntervalOffset class '" + offset.getClass().getCanonicalName() + "' is not supported.");
        }
        
        return object;
    }
    
    /**
     * Creates a {@link SubIntervalOffset} from a {@link JSONObject}.
     * @param object    The JSON object to process.
     * @return The sub interval offset object.
     */
    public static SubIntervalOffset subIntervalOffsetFromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }
        
        String className = object.getClassName();
        if (DayOffset.class.getCanonicalName().equals(className)) {
            int dayCount = object.getValue("dayCount").getIntValue();
            return new DayOffset(dayCount);
        }
        else if (NthDayOfWeekOffset.class.getCanonicalName().equals(className)) {
            DayOfWeek dayOfWeek = object.getValue("dayOfWeek").getEnumValue(DayOfWeek.values());
            int occurrence = object.getValue("occurrence").getIntValue();
            return new NthDayOfWeekOffset(dayOfWeek, occurrence);
        }
        else {
            throw new InvalidContentException("The class name '" + className + "' is not supported.");
        }
    }
    
    /**
     * Creates a {@link SubIntervalOffset} from a {@link JSONValue}.
     * @param value    The JSON value to process.
     * @return The sub interval offset object.
     */
    public static SubIntervalOffset subIntervalOffsetFromJSON(JSONValue value) {
        if (value.isNull()) {
            return null;
        }
        return subIntervalOffsetFromJSON(value.getObjectValue());
    }
    
    
    /**
     * Creates a {@link JSONObject} representing a {@link DateOffset} date offset.
     * Currently only {@link Basic} date offset objects are supported.
     * @param dateOffset    The date offset, may be <code>null</code>, 
     * @return The JSON object, <code>null</code> if dateOffset is <code>null</code>.
     */
    public static JSONObject toJSONObject(DateOffset dateOffset) {
        if (dateOffset == null) {
            return null;
        }
        
        if (!(dateOffset instanceof Basic)) {
            throw new UnsupportedOperationException("The DateOffset class '" + dateOffset.getClass().getCanonicalName() + "' is not supported.");
        }
        return toJSONObject((Basic)dateOffset);
    }
    
    
    /**
     * Creates a {@link JSONObject} representing a {@link Basic} date offset.
     * @param dateOffset    The date offset, may be <code>null</code>, 
     * @return The JSON object, <code>null</code> if dateOffset is <code>null</code>.
     */
    public static JSONObject toJSONObject(Basic dateOffset) {
        if (dateOffset == null) {
            return null;
        }
        
        JSONObject jsonObject = JSONLite.newJSONObject();
        jsonObject.putClassName(dateOffset.getClass());
        jsonObject.add("interval", dateOffset.getInterval());
        jsonObject.add("intervalOffset", dateOffset.getIntervalOffset());
        jsonObject.add("intervalRelation", dateOffset.getIntervalRelation());
        jsonObject.add("subIntervalOffset", toJSONObject(dateOffset.getSubIntervalOffset()));
        jsonObject.add("startOfWeek", dateOffset.getStartOfWeek());
        
        return jsonObject;
    }
    
    /**
     * Creates a {@link Basic} date offset from a {@link JSONObject}.
     * @param jsonObject    The JSON object to interpret.
     * @return The date offset, <code>null</code> if object is <code>null</code>.
     */
    public static Basic basicFromJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        
        String className = jsonObject.getClassName();
        if (!Basic.class.getCanonicalName().equals(className)) {
            throw new InvalidContentException("Could not read the date offset. Expected a '_className' name with a value of '" + Basic.class.getCanonicalName() + "'.");
        }
        
        Interval interval = jsonObject.getValue("interval").getEnumValue(Interval.values());
        int intervalOffset = jsonObject.getValue("intervalOffset").getIntValue();
        IntervalRelation intervalRelation = jsonObject.getValue("intervalRelation").getEnumValue(IntervalRelation.values());
        SubIntervalOffset subIntervalOffset = subIntervalOffsetFromJSON(jsonObject.getValue("subIntervalOffset"));
        DayOfWeek startOfWeek = jsonObject.getValue("startOfWeek").getEnumValue(DayOfWeek.values());
        
        return new Basic(interval, intervalOffset, intervalRelation, subIntervalOffset, startOfWeek);
    }
    
    /**
     * Creates a {@link Basic} date offset from a {@link JSONValue}.
     * @param jsonValue    The JSON value to interpret.
     * @return The date offset, <code>null</code> if value is <code>null</code>.
     */
    public static Basic basicFromJSON(JSONValue jsonValue) {
        if (jsonValue == null) {
            return null;
        }
        return basicFromJSON(jsonValue.getObjectValue());
    }
    
}
