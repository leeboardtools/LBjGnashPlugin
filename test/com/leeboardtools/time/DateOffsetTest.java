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

import com.leeboardtools.json.JSONObject;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class DateOffsetTest {
    
    public DateOffsetTest() {
    }
    
    @Test
    public void testBasicYear() {
        System.out.println("BasicYear");
        
        DateOffset dateOffset;
        LocalDate date;
        dateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 3, 21));
        assertEquals(LocalDate.of(2017, 1, 1), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 2, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 3, 21));
        assertEquals(LocalDate.of(2019, 1, 1), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 2, 
                DateOffset.IntervalRelation.CURRENT_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 3, 21));
        assertEquals(LocalDate.of(2019, 3, 21), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 0, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 3, 21));
        assertEquals(LocalDate.of(2017, 12, 31), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 2, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 3, 21));
        assertEquals(LocalDate.of(2015, 12, 31), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, new DateOffset.DayOffset(4), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 3, 21));
        assertEquals(LocalDate.of(2017, 1, 5), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 0, 
                DateOffset.IntervalRelation.LAST_DAY, new DateOffset.DayOffset(3), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 3, 21));
        assertEquals(LocalDate.of(2017, 12, 28), date);
    }
    
    @Test
    public void testBasicQuarter() {
        System.out.println("BasicQuarter");
        
        DateOffset dateOffset;
        LocalDate date;
        dateOffset = new DateOffset.Basic(DateOffset.Interval.QUARTER, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 4, 1), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.QUARTER, 2, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 10, 1), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.QUARTER, 2, 
                DateOffset.IntervalRelation.CURRENT_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 11, 21), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.QUARTER, 0, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 6, 30), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.QUARTER, 2, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2016, 12, 31), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.QUARTER, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, new DateOffset.DayOffset(4), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 4, 5), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.QUARTER, 0, 
                DateOffset.IntervalRelation.LAST_DAY, new DateOffset.DayOffset(3), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 6, 27), date);
    }
    
    @Test
    public void testBasicMonth() {
        System.out.println("BasicMonth");
        
        DateOffset dateOffset;
        LocalDate date;
        dateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 1), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 2, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 7, 1), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 2, 
                DateOffset.IntervalRelation.CURRENT_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 7, 21), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 6, 21));
        assertEquals(LocalDate.of(2017, 6, 30), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 2, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 9, 21));
        assertEquals(LocalDate.of(2017, 7, 31), date);
        
        // 2017-5-1 was a Monday,.
        dateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, new DateOffset.NthDayOfWeekOffset(DayOfWeek.TUESDAY, 2), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 9), date);
        
        // 2017-5-31 was a Wednesday.
        dateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, 
                DateOffset.IntervalRelation.LAST_DAY, new DateOffset.NthDayOfWeekOffset(DayOfWeek.TUESDAY, 1), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 30), date);
    }
    
    
    @Test
    public void testBasicWeek() {
        System.out.println("BasicWeek");
        
        DateOffset dateOffset;
        LocalDate date;
        
        // 2017-5-21 is a Sunday.
        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 21), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, null, DayOfWeek.TUESDAY);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 16), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 2, 
                DateOffset.IntervalRelation.FIRST_DAY, null, DayOfWeek.TUESDAY);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 30), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 2, 
                DateOffset.IntervalRelation.CURRENT_DAY, null, DayOfWeek.TUESDAY);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 21).plusDays(14), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 0, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 21), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 0, 
                DateOffset.IntervalRelation.LAST_DAY, null, DayOfWeek.TUESDAY);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 23), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 2, 
                DateOffset.IntervalRelation.LAST_DAY, null, DayOfWeek.TUESDAY);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 9), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, new DateOffset.DayOffset(4), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 25), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 0, 
                DateOffset.IntervalRelation.LAST_DAY, new DateOffset.DayOffset(3), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 18), date);

    }

    @Test
    public void testBasicDay() {
        System.out.println("BasicDay");
        
        DateOffset dateOffset;
        LocalDate date;
        dateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 21), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, 2, 
                DateOffset.IntervalRelation.FIRST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 23), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, 2, 
                DateOffset.IntervalRelation.CURRENT_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 23), date);

        dateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, 0, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 21), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, 2, 
                DateOffset.IntervalRelation.LAST_DAY, null, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 19), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, 0, 
                DateOffset.IntervalRelation.FIRST_DAY, new DateOffset.DayOffset(4), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 25), date);
        
        dateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, 0, 
                DateOffset.IntervalRelation.LAST_DAY, new DateOffset.DayOffset(3), null);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 5, 21));
        assertEquals(LocalDate.of(2017, 5, 18), date);
    }
    
    @Test
    public void testPredefined() {
        LocalDate date;
        
        date = LocalDate.of(2017, 3, 15);
        assertEquals(LocalDate.of(2017, 3, 15), DateOffset.SAME_DAY.getOffsetDate(date));
        
        assertEquals(LocalDate.of(2016, 3, 15), DateOffset.TWELVE_MONTHS_PRIOR.getOffsetDate(date));
        assertEquals(LocalDate.of(2015, 3, 15), DateOffset.TWELVE_MONTHS_PRIOR.getOffsetDate(LocalDate.of(2016, 3, 15)));
        
        assertEquals(LocalDate.of(2017, 2, 15), DateOffset.ONE_MONTH_PRIOR.getOffsetDate(date));
        
        assertEquals(LocalDate.of(2017, 1, 1), DateOffset.START_OF_YEAR.getOffsetDate(date));
        assertEquals(LocalDate.of(2017, 12, 31), DateOffset.END_OF_YEAR.getOffsetDate(date));
        assertEquals(LocalDate.of(2017, 3, 1), DateOffset.START_OF_MONTH.getOffsetDate(date));
        assertEquals(LocalDate.of(2017, 3, 31), DateOffset.END_OF_MONTH.getOffsetDate(date));
        assertEquals(LocalDate.of(2016, 12, 31), DateOffset.END_OF_LAST_YEAR.getOffsetDate(date));
        assertEquals(LocalDate.of(2016, 1, 1), DateOffset.START_OF_LAST_YEAR.getOffsetDate(date));
        assertEquals(LocalDate.of(2017, 2, 28), DateOffset.END_OF_LAST_MONTH.getOffsetDate(date));
        assertEquals(LocalDate.of(2017, 2, 1), DateOffset.START_OF_LAST_MONTH.getOffsetDate(date));
    }
    
    
    @Test 
    public void testBasicEquals() {
        DateOffset.Basic dateOffsetA = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY);
        DateOffset.Basic dateOffsetB = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY);
        
        assertEquals(dateOffsetA, dateOffsetB);
        
        dateOffsetB = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.FIRST_DAY);
        assertNotEquals(dateOffsetA, dateOffsetB);
        
        
        dateOffsetA = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.DayOffset(10), DayOfWeek.SATURDAY);
        dateOffsetB = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.DayOffset(10), DayOfWeek.SATURDAY);
        assertEquals(dateOffsetA, dateOffsetB);
        
        dateOffsetB = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.DayOffset(11), DayOfWeek.SATURDAY);
        assertNotEquals(dateOffsetA, dateOffsetB);
        
        
        dateOffsetA = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.NthDayOfWeekOffset(DayOfWeek.FRIDAY, 2), DayOfWeek.SATURDAY);
        dateOffsetB = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.NthDayOfWeekOffset(DayOfWeek.FRIDAY, 2), DayOfWeek.SATURDAY);
        assertEquals(dateOffsetA, dateOffsetB);
        
        dateOffsetB = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.NthDayOfWeekOffset(DayOfWeek.FRIDAY, 2), DayOfWeek.SUNDAY);
        assertNotEquals(dateOffsetA, dateOffsetB);

        dateOffsetB = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.NthDayOfWeekOffset(DayOfWeek.FRIDAY, 1), DayOfWeek.SATURDAY);
        assertNotEquals(dateOffsetA, dateOffsetB);

    }
    
    
    @Test
    public void testJSON() {
        DateOffset.Basic refDateOffset;
        JSONObject jsonObject;
        DateOffset.Basic testDateOffset;
        
        refDateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 10, DateOffset.IntervalRelation.CURRENT_DAY);
        jsonObject = DateOffset.toJSONObject(refDateOffset);
        testDateOffset = DateOffset.basicFromJSON(jsonObject);
        
        assertEquals(refDateOffset, testDateOffset);
        
        
        // Test DateOffset
        refDateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.DayOffset(10), DayOfWeek.SATURDAY);
        jsonObject = DateOffset.toJSONObject(refDateOffset);
        testDateOffset = DateOffset.basicFromJSON(jsonObject);
        
        assertEquals(refDateOffset, testDateOffset);
        
        
        // Test NthDayOfWeekOffset...
        refDateOffset = new DateOffset.Basic(DateOffset.Interval.WEEK, 5, DateOffset.IntervalRelation.LAST_DAY,
            new DateOffset.NthDayOfWeekOffset(DayOfWeek.FRIDAY, 2), DayOfWeek.SATURDAY);
        jsonObject = DateOffset.toJSONObject(refDateOffset);
        testDateOffset = DateOffset.basicFromJSON(jsonObject);
        
        assertEquals(refDateOffset, testDateOffset);
    }
}
