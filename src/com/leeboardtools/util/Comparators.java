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
package com.leeboardtools.util;

/**
 * Some comparison utilities.
 * @author Albert Santos
 */
public class Comparators {
    
    /**
     * Compares two {@link Comparable}s, handling the <code>null</code> case.
     * A <code>null</code> value is considered less than a non-<code>null</code> value.
     * @param <T>   The object type.
     * @param a The first object to compare.
     * @param b The second object to compare.
     * @return &lt; 0 if a is before b, 0 if a is equal to b, &gt; 0 if a is after b.
     */
    public static <T extends Comparable> int compare(T a, T b) {
        if (a == null) {
            return (b == null) ? 0 : -1;
        }
        else if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }
}
