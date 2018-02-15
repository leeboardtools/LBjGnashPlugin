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


/**
 * A calendar quarter.
 * @author Albert Santos
 */
public enum Quarter {
    FIRST(Month.JANUARY, Month.MARCH),
    SECOND(Month.APRIL, Month.JUNE),
    THIRD(Month.JULY, Month.SEPTEMBER),
    FOURTH(Month.OCTOBER, Month.DECEMBER);
    
    
    private final Month firstMonth;
    private final Month lastMonth;
    private Quarter(Month firstMonth, Month lastMonth) {
        this.firstMonth = firstMonth;
        this.lastMonth = lastMonth;
    }
    
    /**
     * @return The first month of the quarter.
     */
    public final Month getFirstMonth() {
        return firstMonth;
    }
    
    /**
     * @return The last month of the quarter.
     */
    public final Month getLastMonth() {
        return lastMonth;
    }
    
    /**
     * Determines if a month is in the quarter.
     * @param month The month of interest.
     * @return <code>true</code> if the month is in the quarter.
     */
    public final boolean isInQuarter(Month month) {
        int value = month.getValue();
        return (firstMonth.getValue() <= value) && (value <= lastMonth.getValue());
    }
    
    /**
     * Determines if a date is in the quarter.
     * @param date  The date of interest.
     * @return <code>true</code> if the date is in the quarter.
     */
    public final boolean isInQuarter(LocalDate date) {
        return isInQuarter(date.getMonth());
    }
    
    private static final Quarter months[] = {
        FIRST,
        FIRST,
        FIRST,
        
        SECOND,
        SECOND,
        SECOND,
        
        THIRD,
        THIRD,
        THIRD,
        
        FOURTH,
        FOURTH,
        FOURTH,
    };
    
    /**
     * Retrieves the quarter containing a given month.
     * @param month The month of interest.
     * @return The quarter.
     */
    public static Quarter of(Month month) {
        return months[month.getValue() - 1];
    }
    
    /**
     * Retrieves the quarter containing a given date.
     * @param date  The date of interest.
     * @return The quarter.
     */
    public static Quarter of(LocalDate date) {
        return of(date.getMonth());
    }
}
