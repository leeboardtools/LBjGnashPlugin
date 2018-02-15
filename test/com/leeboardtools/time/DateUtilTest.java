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
import java.time.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class DateUtilTest {
    
    public DateUtilTest() {
    }

    @Test
    public void testGetOffsetFromStartOfMonth() {
        System.out.println("getOffsetFromStartOfMonth");
        
        LocalDate result;
        result = DateUtil.getOffsetFromStartOfMonth(LocalDate.of(2017, 2, 1), 0);
        assertEquals(LocalDate.of(2017, 2, 1), result);
        
        result = DateUtil.getOffsetFromStartOfMonth(LocalDate.of(2017, 2, 15), 0);
        assertEquals(LocalDate.of(2017, 2, 1), result);

        result = DateUtil.getOffsetFromStartOfMonth(LocalDate.of(2017, 2, 15), 3);
        assertEquals(LocalDate.of(2017, 2, 4), result);

        result = DateUtil.getOffsetFromStartOfMonth(LocalDate.of(2017, 2, 15), 30);
        assertEquals(LocalDate.of(2017, 3, 3), result);
    }

    @Test
    public void testGetStartOfMonth() {
        System.out.println("getStartOfMonth");
        
        LocalDate result;
        result = DateUtil.getStartOfMonth(LocalDate.of(2017, 2, 1));
        assertEquals(LocalDate.of(2017, 2, 1), result);

        result = DateUtil.getStartOfMonth(LocalDate.of(2017, 2, 25));
        assertEquals(LocalDate.of(2017, 2, 1), result);
    }

    @Test
    public void testGetOffsetFromEndOfMonth() {
        System.out.println("getOffsetFromEndOfMonth");
        
        LocalDate result;
        result = DateUtil.getOffsetFromEndOfMonth(LocalDate.of(2016, 2, 29), 0);
        assertEquals(LocalDate.of(2016, 2, 29), result);
        
        result = DateUtil.getOffsetFromEndOfMonth(LocalDate.of(2016, 2, 29), 3);
        assertEquals(LocalDate.of(2016, 2, 26), result);

        result = DateUtil.getOffsetFromEndOfMonth(LocalDate.of(2016, 2, 5), 0);
        assertEquals(LocalDate.of(2016, 2, 29), result);

        result = DateUtil.getOffsetFromEndOfMonth(LocalDate.of(2016, 2, 5), 29);
        assertEquals(LocalDate.of(2016, 1, 31), result);
    }

    @Test
    public void testGetEndOfMonth() {
        System.out.println("getEndOfMonth");
        
        LocalDate result;
        result = DateUtil.getEndOfMonth(LocalDate.of(2016, 2, 29));
        assertEquals(LocalDate.of(2016, 2, 29), result);
        
        result = DateUtil.getEndOfMonth(LocalDate.of(2016, 2, 1));
        assertEquals(LocalDate.of(2016, 2, 29), result);

        result = DateUtil.getEndOfMonth(LocalDate.of(2017, 2, 28));
        assertEquals(LocalDate.of(2017, 2, 28), result);
        
        result = DateUtil.getEndOfMonth(LocalDate.of(2017, 2, 1));
        assertEquals(LocalDate.of(2017, 2, 28), result);
    }

    @Test
    public void testGetOffsetFromStartOfQuarter() {
        System.out.println("getOffsetFromStartOfQuarter");
        
        LocalDate result;
        result = DateUtil.getOffsetFromStartOfQuarter(LocalDate.of(2017, 2, 2), 0);
        assertEquals(LocalDate.of(2017, 1, 1), result);

        result = DateUtil.getOffsetFromStartOfQuarter(LocalDate.of(2017, 2, 2), 10);
        assertEquals(LocalDate.of(2017, 1, 11), result);

        result = DateUtil.getOffsetFromStartOfQuarter(LocalDate.of(2017, 4, 2), 0);
        assertEquals(LocalDate.of(2017, 4, 1), result);

        result = DateUtil.getOffsetFromStartOfQuarter(LocalDate.of(2017, 6, 30), 10);
        assertEquals(LocalDate.of(2017, 4, 11), result);

        result = DateUtil.getOffsetFromStartOfQuarter(LocalDate.of(2017, 7, 1), 0);
        assertEquals(LocalDate.of(2017, 7, 1), result);

        result = DateUtil.getOffsetFromStartOfQuarter(LocalDate.of(2017, 9, 2), 10);
        assertEquals(LocalDate.of(2017, 7, 11), result);

        result = DateUtil.getOffsetFromStartOfQuarter(LocalDate.of(2017, 10, 2), 0);
        assertEquals(LocalDate.of(2017, 10, 1), result);

        result = DateUtil.getOffsetFromStartOfQuarter(LocalDate.of(2017, 12, 31), 10);
        assertEquals(LocalDate.of(2017, 10, 11), result);
    }

    @Test
    public void testGetStartOfQuarter() {
        System.out.println("getStartOfQuarter");
        LocalDate result;
        result = DateUtil.getStartOfQuarter(LocalDate.of(2017, 1, 31));
        assertEquals(LocalDate.of(2017, 1, 1), result);

        result = DateUtil.getStartOfQuarter(LocalDate.of(2017, 3, 31));
        assertEquals(LocalDate.of(2017, 1, 1), result);

        result = DateUtil.getStartOfQuarter(LocalDate.of(2017, 4, 1));
        assertEquals(LocalDate.of(2017, 4, 1), result);

        result = DateUtil.getStartOfQuarter(LocalDate.of(2017, 6, 30));
        assertEquals(LocalDate.of(2017, 4, 1), result);

        result = DateUtil.getStartOfQuarter(LocalDate.of(2017, 7, 1));
        assertEquals(LocalDate.of(2017, 7, 1), result);

        result = DateUtil.getStartOfQuarter(LocalDate.of(2017, 9, 30));
        assertEquals(LocalDate.of(2017, 7, 1), result);

        result = DateUtil.getStartOfQuarter(LocalDate.of(2017, 10, 1));
        assertEquals(LocalDate.of(2017, 10, 1), result);

        result = DateUtil.getStartOfQuarter(LocalDate.of(2017, 12, 31));
        assertEquals(LocalDate.of(2017, 10, 1), result);
    }

    @Test
    public void testGetOffsetFromEndOfQuarter() {
        System.out.println("getOffsetFromEndOfQuarter");
        
        LocalDate result;
        result = DateUtil.getOffsetFromEndOfQuarter(LocalDate.of(2017, 2, 2), 0);
        assertEquals(LocalDate.of(2017, 3, 31), result);

        result = DateUtil.getOffsetFromEndOfQuarter(LocalDate.of(2017, 2, 2), 10);
        assertEquals(LocalDate.of(2017, 3, 21), result);

        result = DateUtil.getOffsetFromEndOfQuarter(LocalDate.of(2017, 4, 2), 0);
        assertEquals(LocalDate.of(2017, 6, 30), result);

        result = DateUtil.getOffsetFromEndOfQuarter(LocalDate.of(2017, 6, 30), 10);
        assertEquals(LocalDate.of(2017, 6, 20), result);

        result = DateUtil.getOffsetFromEndOfQuarter(LocalDate.of(2017, 7, 1), 0);
        assertEquals(LocalDate.of(2017, 9, 30), result);

        result = DateUtil.getOffsetFromEndOfQuarter(LocalDate.of(2017, 9, 2), 10);
        assertEquals(LocalDate.of(2017, 9, 20), result);

        result = DateUtil.getOffsetFromEndOfQuarter(LocalDate.of(2017, 10, 2), 0);
        assertEquals(LocalDate.of(2017, 12, 31), result);

        result = DateUtil.getOffsetFromEndOfQuarter(LocalDate.of(2017, 12, 31), 10);
        assertEquals(LocalDate.of(2017, 12, 21), result);
    }

    @Test
    public void testGetEndOfQuarter() {
        System.out.println("getEndOfQuarter");

        LocalDate result;
        result = DateUtil.getEndOfQuarter(LocalDate.of(2017, 1, 31));
        assertEquals(LocalDate.of(2017, 3, 31), result);

        result = DateUtil.getEndOfQuarter(LocalDate.of(2017, 3, 31));
        assertEquals(LocalDate.of(2017, 3, 31), result);

        result = DateUtil.getEndOfQuarter(LocalDate.of(2017, 4, 1));
        assertEquals(LocalDate.of(2017, 6, 30), result);

        result = DateUtil.getEndOfQuarter(LocalDate.of(2017, 6, 30));
        assertEquals(LocalDate.of(2017, 6, 30), result);

        result = DateUtil.getEndOfQuarter(LocalDate.of(2017, 7, 1));
        assertEquals(LocalDate.of(2017, 9, 30), result);

        result = DateUtil.getEndOfQuarter(LocalDate.of(2017, 9, 30));
        assertEquals(LocalDate.of(2017, 9, 30), result);

        result = DateUtil.getEndOfQuarter(LocalDate.of(2017, 10, 1));
        assertEquals(LocalDate.of(2017, 12, 31), result);

        result = DateUtil.getEndOfQuarter(LocalDate.of(2017, 12, 31));
        assertEquals(LocalDate.of(2017, 12, 31), result);
    }

    @Test
    public void testGetOffsetFromStartOfYear() {
        System.out.println("getOffsetFromStartOfYear");
        LocalDate result;
        result = DateUtil.getOffsetFromStartOfYear(LocalDate.of(2017, 10, 19), 0);
        assertEquals(LocalDate.of(2017, 1, 1), result);

        result = DateUtil.getOffsetFromStartOfYear(LocalDate.of(2017, 10, 19), 10);
        assertEquals(LocalDate.of(2017, 1, 11), result);

        result = DateUtil.getOffsetFromStartOfYear(LocalDate.of(2017, 10, 19), -10);
        assertEquals(LocalDate.of(2016, 12, 22), result);
    }

    @Test
    public void testGetStartOfYear() {
        System.out.println("getStartOfYear");
        LocalDate result;
        result = DateUtil.getStartOfYear(LocalDate.of(2017, 10, 19));
        assertEquals(LocalDate.of(2017, 1, 1), result);
    }

    @Test
    public void testGetOffsetFromEndOfYear() {
        System.out.println("getOffsetFromEndOfYear");
        LocalDate result;
        result = DateUtil.getOffsetFromEndOfYear(LocalDate.of(2017, 10, 19), 0);
        assertEquals(LocalDate.of(2017, 12, 31), result);

        result = DateUtil.getOffsetFromEndOfYear(LocalDate.of(2017, 10, 19), 10);
        assertEquals(LocalDate.of(2017, 12, 21), result);

        result = DateUtil.getOffsetFromEndOfYear(LocalDate.of(2017, 10, 19), -10);
        assertEquals(LocalDate.of(2018, 1, 10), result);
    }

    @Test
    public void testGetEndOfYear() {
        System.out.println("getEndOfYear");
        LocalDate result;
        result = DateUtil.getEndOfYear(LocalDate.of(2017, 10, 19));
        assertEquals(LocalDate.of(2017, 12, 31), result);
    }

    @Test
    public void testGetClosestDayOfWeekOnOrBefore() {
        System.out.println("getClosestDayOfWeekOnOrBefore");
        
        LocalDate result;
        result = DateUtil.getClosestDayOfWeekOnOrBefore(LocalDate.of(2018, 2, 10), DayOfWeek.SATURDAY);
        assertEquals(LocalDate.of(2018, 2, 10), result);

        result = DateUtil.getClosestDayOfWeekOnOrBefore(LocalDate.of(2018, 2, 10), DayOfWeek.FRIDAY);
        assertEquals(LocalDate.of(2018, 2, 9), result);

        result = DateUtil.getClosestDayOfWeekOnOrBefore(LocalDate.of(2018, 2, 10), DayOfWeek.SUNDAY);
        assertEquals(LocalDate.of(2018, 2, 4), result);
    }

    @Test
    public void testGetClosestDayOfWeekOnOrAfter() {
        System.out.println("getClosestDayOfWeekOnOrAfter");
        
        LocalDate result;
        result = DateUtil.getClosestDayOfWeekOnOrAfter(LocalDate.of(2018, 2, 10), DayOfWeek.SATURDAY);
        assertEquals(LocalDate.of(2018, 2, 10), result);

        result = DateUtil.getClosestDayOfWeekOnOrAfter(LocalDate.of(2018, 2, 10), DayOfWeek.FRIDAY);
        assertEquals(LocalDate.of(2018, 2, 16), result);

        result = DateUtil.getClosestDayOfWeekOnOrAfter(LocalDate.of(2018, 2, 10), DayOfWeek.SUNDAY);
        assertEquals(LocalDate.of(2018, 2, 11), result);
    }
    
}
