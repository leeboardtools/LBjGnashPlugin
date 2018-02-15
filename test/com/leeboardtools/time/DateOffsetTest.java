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
public class DateOffsetTest {
    
    public DateOffsetTest() {
    }
    
    DateOffset.StandardDateOffset createStandardDateOffset(DateOffset.Standard standard, int [] parameters) {
        DateOffset.StandardDateOffset dateOffset = DateOffset.fromStandard(standard, parameters, 0);
        
        assertEquals(standard, dateOffset.getStandard());
        assertArrayEquals(parameters, dateOffset.getParameters());
        
        return dateOffset;
    }

    @Test
    public void testNull() {
        System.out.println("Null");

        DateOffset dateOffset;
        LocalDate date;
        dateOffset = createStandardDateOffset(DateOffset.Standard.NULL, new int [] {});
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 2, 12));
        assertEquals(LocalDate.of(2017, 2, 12), date);
        
    }


    @Test
    public void testDaysOffset() {
        System.out.println("DaysOffset");

        DateOffset.StandardDateOffset dateOffset;
        LocalDate date;
        dateOffset = new DateOffset.DaysOffset(-5);
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 2, 12));
        assertEquals(LocalDate.of(2017, 2, 7), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2017, 2, 12));
        assertEquals(LocalDate.of(2017, 2, 7), date);
        
    }

    @Test
    public void testNthDayOfWeek() {
        System.out.println("NthDayOfWeek");

        DateOffset.StandardDateOffset dateOffset;
        LocalDate date;
        
        dateOffset = new DateOffset.NthDayOfWeek(DayOfWeek.MONDAY, 2);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 2, 19), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 2, 19), date);
        
        dateOffset = new DateOffset.NthDayOfWeek(DayOfWeek.SUNDAY, 3);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 3, 4), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 3, 4), date);
        
        dateOffset = new DateOffset.NthDayOfWeek(DayOfWeek.TUESDAY, 1);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 2, 13), date);
    }

    @Test
    public void testWeeksOffset() {
        System.out.println("WeeksOffset");

        DateOffset.StandardDateOffset dateOffset;
        LocalDate date;
        
        dateOffset = new DateOffset.WeeksOffset(DayOfWeek.SUNDAY, -2, 3, DateOffset.OffsetReference.FIRST_DAY);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 1, 31), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 1, 31), date);
        
        dateOffset = new DateOffset.WeeksOffset(DayOfWeek.MONDAY, -3, 2, DateOffset.OffsetReference.LAST_DAY);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 3, 2), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 3, 2), date);
        
    }

    @Test
    public void testMonthsOffset() {
        System.out.println("MonthsOffset");

        DateOffset.StandardDateOffset dateOffset;
        LocalDate date;
        
        dateOffset = new DateOffset.MonthsOffset(-2, 3, DateOffset.OffsetReference.FIRST_DAY, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2017, 12, 4), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2017, 12, 4), date);
        
        dateOffset = new DateOffset.MonthsOffset(-3, 2, DateOffset.OffsetReference.LAST_DAY, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 5, 29), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 5, 29), date);
        
        // The second Wednesday...
        DateOffset.StandardDateOffset subDateOffset = new DateOffset.NthDayOfWeek(DayOfWeek.WEDNESDAY, 2);

        dateOffset = new DateOffset.MonthsOffset(-2, 0, DateOffset.OffsetReference.FIRST_DAY, subDateOffset);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2017, 12, 13), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2017, 12, 13), date);
    }

    @Test
    public void testQuartersOffset() {
        System.out.println("QuartersOffset");

        DateOffset.StandardDateOffset dateOffset;
        LocalDate date;
        
        dateOffset = new DateOffset.QuartersOffset(-2, 3, DateOffset.OffsetReference.FIRST_DAY, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2017, 7, 4), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2017, 7, 4), date);
        
        dateOffset = new DateOffset.QuartersOffset(-3, 2, DateOffset.OffsetReference.LAST_DAY, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 12, 29), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2018, 12, 29), date);
        
        // The second Wednesday...
        DateOffset.StandardDateOffset subDateOffset = new DateOffset.NthDayOfWeek(DayOfWeek.WEDNESDAY, 2);

        dateOffset = new DateOffset.QuartersOffset(-2, 0, DateOffset.OffsetReference.FIRST_DAY, subDateOffset);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2017, 7, 12), date);

        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2017, 7, 12), date);
    }

    @Test
    public void testYearsOffset() {
        System.out.println("YearsOffset");


        DateOffset.StandardDateOffset dateOffset;
        LocalDate date;
        
        dateOffset = new DateOffset.YearsOffset(-2, 3, DateOffset.OffsetReference.FIRST_DAY, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2016, 1, 4), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2016, 1, 4), date);
        
        dateOffset = new DateOffset.YearsOffset(-3, 2, DateOffset.OffsetReference.LAST_DAY, null);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2021, 12, 29), date);
        
        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2021, 12, 29), date);
        
        // The second Wednesday...
        DateOffset.StandardDateOffset subDateOffset = new DateOffset.NthDayOfWeek(DayOfWeek.WEDNESDAY, 2);

        dateOffset = new DateOffset.YearsOffset(-2, 0, DateOffset.OffsetReference.FIRST_DAY, subDateOffset);
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2016, 1, 13), date);

        dateOffset = createStandardDateOffset(dateOffset.getStandard(), dateOffset.getParameters());
        date = dateOffset.getOffsetDate(LocalDate.of(2018, 2, 12));
        assertEquals(LocalDate.of(2016, 1, 13), date);
    }
    
}
