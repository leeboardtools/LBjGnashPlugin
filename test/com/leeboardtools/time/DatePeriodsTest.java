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

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Iterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class DatePeriodsTest {
    
    public DatePeriodsTest() {
    }
    
    void checkIterator(Iterator<DateRange> iterator, LocalDate ... dates) {
        int count = 0;
        while (iterator.hasNext()) {
            DateRange range = iterator.next();
            assertEquals(dates[count], range.getOldestDate());
            assertEquals(dates[count], range.getNewestDate());
            ++count;
        }
        assertEquals(count, dates.length);
    }
    
    void checkIterator(Iterator<DateRange> iterator, DateRange ... refRanges) {
        int count = 0;
        while (iterator.hasNext()) {
            DateRange range = iterator.next();
            assertEquals(refRanges[count], range);
            ++count;
        }
        assertEquals(count, refRanges.length);
    }

    @Test
    public void testIterator() {
        System.out.println("iterator");
        
        Period period = PeriodUtil.fromStandard(PeriodUtil.Standard.QUARTER, 1);
        int periodCount = 3;
        DateOffset startDateOffset = null;
        DateRange.Generator rangeGenerator = null;
        DateOffset rangeOffset = null;
        
        DatePeriods periods = new DatePeriods(period, periodCount, startDateOffset, rangeGenerator, rangeOffset);
        Iterator<DateRange> iterator = periods.iterator(LocalDate.of(2018, 2, 11));
        checkIterator(iterator,
                LocalDate.of(2018, 2, 11),
                LocalDate.of(2017, 11, 11),
                LocalDate.of(2017, 8, 11));
        
        periodCount = 5;
        startDateOffset = new DateOffset.YearsOffset(1, 0, DateOffset.OffsetReference.LAST_DAY, null);
        rangeOffset = new DateOffset.MonthsOffset(0, 0, DateOffset.OffsetReference.LAST_DAY, null);
        periods = new DatePeriods(period, periodCount, startDateOffset, rangeGenerator, rangeOffset);
        iterator = periods.iterator(LocalDate.of(2018, 2, 11));
        checkIterator(iterator,
                LocalDate.of(2017, 12, 31),
                LocalDate.of(2017, 9, 30),
                LocalDate.of(2017, 6, 30),
                LocalDate.of(2017, 3, 31),
                LocalDate.of(2016, 12, 31));
    }
    
}
