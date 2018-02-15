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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class DateRangeTest {
    
    public DateRangeTest() {
    }

    @Test
    public void testIsDateInRange() {
        System.out.println("isDateInRange");
        
        DateRange range = new DateRange(LocalDate.of(2017, 10, 20), LocalDate.of(2017, 9, 10));
        assertFalse(range.isDateInRange(LocalDate.of(2017, 9, 9)));
        assertTrue(range.isDateInRange(LocalDate.of(2017, 9, 10)));
        assertTrue(range.isDateInRange(LocalDate.of(2017, 10, 1)));
        assertTrue(range.isDateInRange(LocalDate.of(2017, 10, 20)));
        assertFalse(range.isDateInRange(LocalDate.of(2017, 10, 21)));
    }

    @Test
    public void testFromEdgeDates() {
        System.out.println("fromEdgeDates");

        DateRange range = DateRange.fromEdgeDates(LocalDate.of(2017, 9, 10), LocalDate.of(2017, 10, 20));
        assertFalse(range.isDateInRange(LocalDate.of(2017, 9, 9)));
        assertTrue(range.isDateInRange(LocalDate.of(2017, 9, 10)));
        assertTrue(range.isDateInRange(LocalDate.of(2017, 10, 1)));
        assertTrue(range.isDateInRange(LocalDate.of(2017, 10, 20)));
        assertFalse(range.isDateInRange(LocalDate.of(2017, 10, 21)));
    }
    
    DateRange.StandardGenerator createGenerator(DateRange.Standard standard, int parameters[]) {
        DateRange.StandardGenerator generator = DateRange.fromStandard(standard, parameters);
        assertEquals(standard, generator.getStandard());
        assertArrayEquals(parameters, generator.getParameters());
        return generator;
    }

    @Test
    public void testFromCurrentYear() {
        System.out.println("fromCurrentYear");
        
        DateRange range = DateRange.fromCurrentYear(LocalDate.of(2018, 2, 11), 2);
        assertEquals(LocalDate.of(2017, 1, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 12, 31), range.getNewestDate());
        
        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.CURRENT_YEAR, new int [] { 2 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2017, 1, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 12, 31), range.getNewestDate());
    }

    @Test
    public void testFromCurrentMonth() {
        System.out.println("fromCurrentMonth");
        
        DateRange range = DateRange.fromCurrentMonth(LocalDate.of(2016, 2, 11), 3);
        assertEquals(LocalDate.of(2015, 12, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2016, 2, 29), range.getNewestDate());
        
        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.CURRENT_MONTH, new int [] { 3 });
        range = generator.generateRange(LocalDate.of(2016, 2, 11));
        assertEquals(LocalDate.of(2015, 12, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2016, 2, 29), range.getNewestDate());
    }

    @Test
    public void testFromCurrentQuarter() {
        System.out.println("fromCurrentQuarter");
        
        DateRange range = DateRange.fromCurrentQuarter(LocalDate.of(2016, 2, 11), 2);
        assertEquals(LocalDate.of(2015, 10, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2016, 3, 31), range.getNewestDate());
        
        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.CURRENT_QUARTER, new int [] { 2 });
        range = generator.generateRange(LocalDate.of(2016, 2, 11));
        assertEquals(LocalDate.of(2015, 10, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2016, 3, 31), range.getNewestDate());
    }

    @Test
    public void testFromCurrentWeek() {
        System.out.println("fromCurrentWeek");
        
        DateRange range = DateRange.fromCurrentWeek(LocalDate.of(2018, 2, 12), null, 3);
        assertEquals(LocalDate.of(2018, 1, 28), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 17), range.getNewestDate());
        
        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.CURRENT_WEEK, new int [] { -1, 3 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2018, 1, 28), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 17), range.getNewestDate());

        range = DateRange.fromCurrentWeek(LocalDate.of(2018, 2, 12), DayOfWeek.FRIDAY, 1);
        assertEquals(LocalDate.of(2018, 2, 9), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 15), range.getNewestDate());
        
        generator = createGenerator(DateRange.Standard.CURRENT_WEEK, new int [] { DayOfWeek.FRIDAY.getValue(), 1 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2018, 2, 9), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 15), range.getNewestDate());
    }

    @Test
    public void testFromYearToDate() {
        System.out.println("fromYearToDate");
        
        DateRange range = DateRange.fromYearToDate(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2018, 1, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());
        
        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.YEAR_TO_DATE, new int [0]);
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2018, 1, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());
    }

    @Test
    public void testFromMonthToDate() {
        System.out.println("fromMonthToDate");
        
        DateRange range = DateRange.fromMonthToDate(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2018, 2, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.MONTH_TO_DATE, new int [0]);
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2018, 2, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());
    }

    @Test
    public void testFromQuarterToDate() {
        System.out.println("fromQuarterToDate");
        
        DateRange range = DateRange.fromQuarterToDate(LocalDate.of(2018, 12, 11));
        assertEquals(LocalDate.of(2018, 10, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 12, 11), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.QUARTER_TO_DATE, new int [0]);
        range = generator.generateRange(LocalDate.of(2018, 12, 11));
        assertEquals(LocalDate.of(2018, 10, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 12, 11), range.getNewestDate());
    }

    @Test
    public void testLastYears() {
        System.out.println("lastYears");
        
        DateRange range = DateRange.lastYears(LocalDate.of(2018, 2, 11), 3);
        assertEquals(LocalDate.of(2015, 2, 12), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.LAST_YEARS, new int [] { 3 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2015, 2, 12), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());
    }

    @Test
    public void testLastMonths() {
        System.out.println("lastMonths");
        
        DateRange range = DateRange.lastMonths(LocalDate.of(2018, 2, 11), 3);
        assertEquals(LocalDate.of(2017, 11, 12), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.LAST_MONTHS, new int [] { 3 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2017, 11, 12), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());
    }

    @Test
    public void testLastWeeks() {
        System.out.println("lastWeeks");
        
        DateRange range = DateRange.lastWeeks(LocalDate.of(2018, 2, 11), 3);
        assertEquals(LocalDate.of(2018, 1, 22), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.LAST_WEEKS, new int [] { 3 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2018, 1, 22), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());
    }

    @Test
    public void testLastDays() {
        System.out.println("lastDays");
        
        DateRange range = DateRange.lastDays(LocalDate.of(2018, 2, 11), 3);
        assertEquals(LocalDate.of(2018, 2, 9), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.LAST_DAYS, new int [] { 3 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2018, 2, 9), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 2, 11), range.getNewestDate());
    }

    @Test
    public void testPreceedingYears() {
        System.out.println("preceedingYears");
        
        DateRange range = DateRange.preceedingYears(LocalDate.of(2018, 2, 11), 3);
        assertEquals(LocalDate.of(2015, 1, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2017, 12, 31), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.PRECEEDING_YEARS, new int [] { 3 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2015, 1, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2017, 12, 31), range.getNewestDate());
    }

    @Test
    public void testPreceedingMonths() {
        System.out.println("preceedingMonths");
        
        DateRange range = DateRange.preceedingMonths(LocalDate.of(2018, 2, 11), 3);
        assertEquals(LocalDate.of(2017, 11, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 1, 31), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.PRECEEDING_MONTHS, new int [] { 3 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2017, 11, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2018, 1, 31), range.getNewestDate());
    }

    @Test
    public void testPreceedingQuarters() {
        System.out.println("preceedingQuarters");
        
        DateRange range = DateRange.preceedingQuarters(LocalDate.of(2018, 2, 11), 3);
        assertEquals(LocalDate.of(2017, 4, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2017, 12, 31), range.getNewestDate());

        DateRange.StandardGenerator generator = createGenerator(DateRange.Standard.PRECEEDING_QUARTERS, new int [] { 3 });
        range = generator.generateRange(LocalDate.of(2018, 2, 11));
        assertEquals(LocalDate.of(2017, 4, 1), range.getOldestDate());
        assertEquals(LocalDate.of(2017, 12, 31), range.getNewestDate());
    }
    
}
