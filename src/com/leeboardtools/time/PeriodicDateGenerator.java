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

import com.leeboardtools.json.JSONLite;
import com.leeboardtools.json.JSONObject;
import com.leeboardtools.json.JSONValue;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Class that describes a periodic sequence of {@link LocalDate}s using {@link DateOffset}s.
 * The dates are obtained via an {@link Iterator} given a reference date.
 * This class is immutable.
 * @author Albert Santos
 */
public class PeriodicDateGenerator {
    private final DateOffset startDateOffset;
    private final DateOffset.Basic periodDateOffset;
    private final int periodCount;
    private final DateOffset endDateOffset;
    
    /**
     * Constructor.
     * @param startDateOffset   The date offset describing the date of the first period, not <code>null</code>.
     * @param periodDateOffset  The date offset describing the offset of the start date of each succeeding period.
     * A clone of this is made each period with an increase in interval offset from the previous period and
     * applied to the date of the first period. Not <code>null</code>.
     * @param periodCount   The number of periods, this must be 0 if endDateOffset is not <code>null</code>.
     * @param endDateOffset If not <code>null</code> the date offset describing the end date. If the resolved
     * end date is in the opposite direction of the periodDateOffset, a single period is generated.
     */
    public PeriodicDateGenerator(DateOffset startDateOffset, DateOffset.Basic periodDateOffset, int periodCount, DateOffset endDateOffset) {
        Objects.requireNonNull(startDateOffset);
        Objects.requireNonNull(periodDateOffset);
        if ((periodCount != 0) && (endDateOffset != null)) {
            throw new IllegalArgumentException("periodCount must be 0 if endDateOffset is not null!");
        }
        this.startDateOffset = startDateOffset;
        this.periodDateOffset = periodDateOffset;
        this.periodCount = periodCount;
        this.endDateOffset = endDateOffset;
    }
    
    
    /**
     * Constructor.
     * @param startDateOffset   The date offset describing the date of the first period, not <code>null</code>.
     * @param periodDateOffset  The date offset describing the offset of the start date of each succeeding period.
     * A clone of this is made each period with an increase in interval offset from the previous period and
     * applied to the date of the first period. Not <code>null</code>.
     * @param periodCount   The number of periods, a value of 0 generates a single date, 1 generates two dates...
     */
    public PeriodicDateGenerator(DateOffset startDateOffset, DateOffset.Basic periodDateOffset, int periodCount) {
        this(startDateOffset, periodDateOffset, periodCount, null);
    }
    
    
    /**
     * Constructor.
     * @param startDateOffset   The date offset describing the date of the first period, not <code>null</code>.
     * @param periodDateOffset  The date offset describing the offset of the start date of each succeeding period.
     * A clone of this is made each period with an increase in interval offset from the previous period and
     * applied to the date of the first period. Not <code>null</code>.
     * @param endDateOffset The date offset describing the end date. If the resolved
     * end date is in the opposite direction of the periodDateOffset, a single period is generated, not <code>null</code>.
     */
    public PeriodicDateGenerator(DateOffset startDateOffset, DateOffset.Basic periodDateOffset, DateOffset endDateOffset) {
        this(startDateOffset, periodDateOffset, 0, endDateOffset);
        Objects.requireNonNull(endDateOffset);
    }
    
    /**
     * @return The date offset describing the date of the first period.
     */
    public final DateOffset getStartDateOffset() {
        return startDateOffset;
    }
    
    /**
     * @return The date offset describing the offset of the start date of each succeeding period
     * relative to the start date of the previous period.
     */
    public final DateOffset.Basic getPeriodDateOffset() {
        return periodDateOffset;
    }
    
    /**
     * @return The number of periods to generated, 0 if there's an end date offset.
     */
    public final int getPeriodCount() {
        return periodCount;
    }
    
    /**
     * @return The date offset describing the end date of the periods to generate, this
     * is applied to the reference date just like {@link #getStartDateOffset() }. This is
     * inclusive.
     */
    public final DateOffset getEndDateOffset() {
        return endDateOffset;
    }
    
    
    /**
     * Retrieves an iterator for generating the periodic dates.
     * @param refDate   The reference date, the start date offset is applied to this
     * date to obtain the first date returned. If {@link #getEndDateOffset() } is not <code>null</code>
     * it is applied to this date to obtain the terminating date.
     * @return The iterator.
     */
    public Iterator<LocalDate> getIterator(LocalDate refDate) {
        if (endDateOffset == null) {
            return new PeriodCountIterator(refDate);
        }
        else {
            return new EndDateIterator(refDate);
        }
    }
    
    
    /**
     * Populates a collection with the periodic dates.
     * @param refDate   The reference date.
     * @param dates The collection to be populated. The collection is not cleared...
     */
    public void getPeriodicDates(LocalDate refDate, Collection<LocalDate> dates) {
        Iterator<LocalDate> iterator = getIterator(refDate);
        while (iterator.hasNext()) {
            dates.add(iterator.next());
        }
    }

    
    protected abstract class IteratorBase implements Iterator<LocalDate> {
        final LocalDate startDate;
        LocalDate currentDate;
        final int originalIntervalCount;
        DateOffset.Basic currentPeriodDateOffset;
        
        IteratorBase(LocalDate refDate) {
            startDate = startDateOffset.getOffsetDate(refDate);
            currentDate = startDate;
            originalIntervalCount = periodDateOffset.getIntervalOffset();
            currentPeriodDateOffset = periodDateOffset;
        }
        
        void advanceCurrentDate() {
            currentDate = currentPeriodDateOffset.getOffsetDate(startDate);
            currentPeriodDateOffset = currentPeriodDateOffset.plusIntervalOffset(originalIntervalCount);
        }
    }
    
    protected class PeriodCountIterator extends IteratorBase {
        private int periodsRemaining;
        
        PeriodCountIterator(LocalDate refDate) {
            super(refDate);
            periodsRemaining = periodCount + 1;
        }

        @Override
        public boolean hasNext() {
            return (periodsRemaining > 0);
        }

        @Override
        public LocalDate next() {
            if (periodsRemaining <= 0) {
                throw new NoSuchElementException();
            }
            
            LocalDate returnDate = currentDate;
            --periodsRemaining;
            if (periodsRemaining > 0) {
                advanceCurrentDate();
            }
            
            return returnDate;
        }
    }
    
    
    protected class EndDateIterator extends IteratorBase {
        private LocalDate endDate;
        private final boolean isTowardsFuture;
        
        EndDateIterator(LocalDate refDate) {
            super(refDate);
            LocalDate nextDate = periodDateOffset.getOffsetDate(currentDate);
            endDate = endDateOffset.getOffsetDate(refDate);
            
            long deltaDays = DateUtil.daysTo(currentDate, nextDate);
            long deltaEndDays = DateUtil.daysTo(currentDate, endDate);
            
            if (deltaDays > 0) {
                isTowardsFuture = true;
            }
            else {
                isTowardsFuture = false;
            }
            
            if (deltaDays * deltaEndDays < 0) {
                // Direction of periods is opposite end date direction, we're only
                // going to have one period.
                endDate = currentDate;
            }
        }

        @Override
        public boolean hasNext() {
            if (isTowardsFuture) {
                return !currentDate.isAfter(endDate);
            }
            else {
                return !currentDate.isBefore(endDate);
            }
        }

        @Override
        public LocalDate next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            LocalDate returnDate = currentDate;
            advanceCurrentDate();
            return returnDate;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.startDateOffset);
        hash = 29 * hash + Objects.hashCode(this.periodDateOffset);
        hash = 29 * hash + this.periodCount;
        hash = 29 * hash + Objects.hashCode(this.endDateOffset);
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
        final PeriodicDateGenerator other = (PeriodicDateGenerator) obj;
        if (this.periodCount != other.periodCount) {
            return false;
        }
        if (!Objects.equals(this.startDateOffset, other.startDateOffset)) {
            return false;
        }
        if (!Objects.equals(this.periodDateOffset, other.periodDateOffset)) {
            return false;
        }
        if (!Objects.equals(this.endDateOffset, other.endDateOffset)) {
            return false;
        }
        return true;
    }
    
    
    /**
     * Creates a {@link JSONObject} representing a {@link PeriodicDateGenerator} object.
     * @param generator The generator, may be <code>null</code>.
     * @return The JSON object, <code>null</code> if generator is <code>null</code>.
     */
    public static JSONObject toJSONObject(PeriodicDateGenerator generator) {
        if (generator == null) {
            return null;
        }
        
        JSONObject jsonObject = JSONLite.newJSONObject();
        jsonObject.putClassName(PeriodicDateGenerator.class);
        jsonObject.add("startDateOffset", DateOffset.toJSONObject(generator.getStartDateOffset()));
        jsonObject.add("periodDateOffset", DateOffset.toJSONObject(generator.getPeriodDateOffset()));
        jsonObject.add("periodCount", generator.getPeriodCount());
        jsonObject.add("endDateOffset", DateOffset.toJSONObject(generator.getEndDateOffset()));

        return jsonObject;
    }
    
    /**
     * Creates a {@link PeriodicDateGenerator} from a {@link JSONObject}.
     * @param jsonObject    The JSON object.
     * @return The periodic date generator, <code>null</code> if object is <code>null</code>.
     */
    public static PeriodicDateGenerator fromJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        
        jsonObject.verifyClass(PeriodicDateGenerator.class);
        DateOffset.Basic startDateOffset = DateOffset.basicFromJSON(jsonObject.getValue("startDateOffset"));
        DateOffset.Basic periodDateOffset = DateOffset.basicFromJSON(jsonObject.getValue("periodDateOffset"));
        int periodCount = jsonObject.getValue("periodCount").getIntValue();
        DateOffset.Basic endDateOffset = DateOffset.basicFromJSON(jsonObject.getValue("endDateOffset"));
        
        return new PeriodicDateGenerator(startDateOffset, periodDateOffset, periodCount, endDateOffset);
    }
    
    
    /**
     * Creates a {@link PeriodicDateGenerator} from a {@link JSONValue}.
     * @param jsonValue    The JSON value.
     * @return The periodic date generator, <code>null</code> if value is <code>null</code>.
     */
    public static PeriodicDateGenerator fromJSON(JSONValue jsonValue) {
        if (jsonValue == null) {
            return null;
        }
        return fromJSON(jsonValue.getObjectValue());
    }
}
