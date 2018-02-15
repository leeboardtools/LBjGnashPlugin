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

import java.time.Period;

/**
 * Some helpers for working with {@link Period}.
 * @author Albert Santos
 */
public class PeriodUtil {
    
    /**
     * Some standard pre-defined periods.
     */
    public static enum Standard {
        DAY,
        WEEK,
        MONTH,
        QUARTER,
        YEAR,
    }
    
    /**
     * Creates a {@link Period} from one of the pre-defined periods.
     * @param standard    The standard.
     * @param multiplier    The multiplier, this should not be 0. Positive advances into
     * the future, negative advances into the past.
     * @return The period.
     */
    public static Period fromStandard(Standard standard, int multiplier) {
        switch (standard) {
            case DAY :
                return Period.ofDays(multiplier);
            case WEEK :
                return Period.ofWeeks(multiplier);
            case MONTH :
                return Period.ofMonths(multiplier);
            case QUARTER :
                return Period.ofMonths(multiplier * 3);
            case YEAR :
                return Period.ofYears(multiplier);
        }
        
        throw new IllegalArgumentException("period not recognized.");
    }
}
