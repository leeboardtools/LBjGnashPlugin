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
import java.time.LocalDate;
import java.time.Month;
import java.util.Iterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class PeriodicDateGeneratorTest {
    
    public PeriodicDateGeneratorTest() {
    }
    
    void checkDates(LocalDate refDate, LocalDate [] refPeriodicDates, PeriodicDateGenerator generator) {
        Iterator<LocalDate> iterator = generator.getIterator(refDate);
        
        int count = refPeriodicDates.length;
        for (int i = 0; i < count; ++i) {
            assertTrue(iterator.hasNext());
            LocalDate date = iterator.next();
            assertEquals(refPeriodicDates[i], date);
        }
        
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testPeriodCount() {
        System.out.println("PeriodCount");
        PeriodicDateGenerator generator;
        DateOffset startDateOffset;
        DateOffset.Basic periodDateOffset;
        int periodCount;
        LocalDate refDate;
        
        startDateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, DateOffset.IntervalRelation.FIRST_DAY);
        periodDateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 1, DateOffset.IntervalRelation.LAST_DAY);
        periodCount = 5;
        generator = new PeriodicDateGenerator(startDateOffset, periodDateOffset, periodCount);
        
        refDate = LocalDate.of(2018, Month.MARCH, 3);
        checkDates(refDate, new LocalDate[] {
                    LocalDate.of(2018, 3, 1),
                    LocalDate.of(2017, 12, 31),
                    LocalDate.of(2016, 12, 31),
                    LocalDate.of(2015, 12, 31),
                    LocalDate.of(2014, 12, 31),
                    LocalDate.of(2013, 12, 31),
                },
                generator);
        
        // 0 periods.
        periodCount = 0;
        generator = new PeriodicDateGenerator(startDateOffset, periodDateOffset, periodCount);
        
        refDate = LocalDate.of(2018, Month.MARCH, 3);
        checkDates(refDate, new LocalDate[] {
                    LocalDate.of(2018, 3, 1),
                },
                generator);

        
        startDateOffset = DateOffset.SAME_DAY;
        periodDateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, -2, DateOffset.IntervalRelation.CURRENT_DAY);
        periodCount = 5;
        generator = new PeriodicDateGenerator(startDateOffset, periodDateOffset, periodCount);
        
        refDate = LocalDate.of(2018, 3, 12);
        checkDates(refDate, new LocalDate[] {
                    LocalDate.of(2018, 3, 12),
                    LocalDate.of(2016, 3, 12),
                    LocalDate.of(2014, 3, 12),
                    LocalDate.of(2012, 3, 12),
                    LocalDate.of(2010, 3, 12),
                    LocalDate.of(2008, 3, 12),
                },
                generator);
    }

    @Test
    public void testEndDateOffset() {
        System.out.println("EndDateOffset");
        PeriodicDateGenerator generator;
        DateOffset startDateOffset;
        DateOffset.Basic periodDateOffset;
        DateOffset endDateOffset;
        LocalDate refDate;
        
        startDateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, DateOffset.IntervalRelation.FIRST_DAY);
        periodDateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 1, DateOffset.IntervalRelation.LAST_DAY);
        endDateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 5, DateOffset.IntervalRelation.LAST_DAY);

        generator = new PeriodicDateGenerator(startDateOffset, periodDateOffset, endDateOffset);
        
        refDate = LocalDate.of(2018, Month.MARCH, 3);
        checkDates(refDate, new LocalDate[] {
                    LocalDate.of(2018, 3, 1),
                    LocalDate.of(2017, 12, 31),
                    LocalDate.of(2016, 12, 31),
                    LocalDate.of(2015, 12, 31),
                    LocalDate.of(2014, 12, 31),
                    LocalDate.of(2013, 12, 31),
                },
                generator);
        

        // End date in the opposite direction...
        endDateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, -1, DateOffset.IntervalRelation.LAST_DAY);
        generator = new PeriodicDateGenerator(startDateOffset, periodDateOffset, endDateOffset);
        
        refDate = LocalDate.of(2018, Month.MARCH, 3);
        checkDates(refDate, new LocalDate[] {
                    LocalDate.of(2018, 3, 1),
                },
                generator);
    }
    
    
//    public static PeriodicDateGenerator SAME_DAY = new PeriodicDateGenerator(new DateOffset())

    @Test
    public void testJSON() {
        PeriodicDateGenerator refGenerator;
        JSONObject jsonObject;
        PeriodicDateGenerator testGenerator;

        DateOffset startDateOffset;
        DateOffset.Basic periodDateOffset;
        DateOffset endDateOffset;
        
        startDateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, DateOffset.IntervalRelation.FIRST_DAY);
        periodDateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 1, DateOffset.IntervalRelation.LAST_DAY);
        endDateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 5, DateOffset.IntervalRelation.LAST_DAY);

        refGenerator = new PeriodicDateGenerator(startDateOffset, periodDateOffset, endDateOffset);
        jsonObject = PeriodicDateGenerator.toJSONObject(refGenerator);
        testGenerator = PeriodicDateGenerator.fromJSONObject(jsonObject);
        
        assertEquals(refGenerator, testGenerator);
        
        
        startDateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, DateOffset.IntervalRelation.FIRST_DAY);
        periodDateOffset = new DateOffset.Basic(DateOffset.Interval.YEAR, 1, DateOffset.IntervalRelation.LAST_DAY);
        endDateOffset = null;

        refGenerator = new PeriodicDateGenerator(startDateOffset, periodDateOffset, 5, endDateOffset);
        jsonObject = PeriodicDateGenerator.toJSONObject(refGenerator);
        testGenerator = PeriodicDateGenerator.fromJSONObject(jsonObject);
        
        assertEquals(refGenerator, testGenerator);
    }
}
