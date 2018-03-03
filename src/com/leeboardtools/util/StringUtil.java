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

import java.util.Collection;
import java.util.Set;
import javafx.util.Callback;

/**
 * Utility methods for working with strings.
 * @author Albert Santos
 */
public class StringUtil {
    
    /**
     * Determines if a string is both non-<code>null</code> and non-empty.
     * @param s The string of interest.
     * @return <code>true</code> if s is both non-<code>null</code> and {@link String#isEmpty() } returns <code>false</code>
     */
    public static boolean isNonEmpty(String s) {
        return (s != null) && !s.isEmpty();
    }
    
    
    /**
     * Determines if a string is unique via a callback, and if it is not, retrieves a string that is unique.
     * @param s The string of interest, not <code>null</code>.
     * @param isUniqueCallback  The callback, its {@link Callback#call(java.lang.Object) } should return <code>true</code>
     * if the argument is unique.
     * @param separator The separator used to separate the original string from appended digits, if <code>null</code>
     * a default separator will be used.
     * @return The unique string.
     */
    public static String getUniqueString(String s, Callback<String, Boolean> isUniqueCallback, String separator) {
        if (isUniqueCallback.call(s)) {
            return s;
        }
        
        if (separator == null) {
            separator = " - ";
        }
        
        // If the end of s ends in '- number' we'll presume it's an increment number, and we'll
        // increment that.
        int index = s.lastIndexOf(separator);
        if (index >= 0) {
            String trailing = s.substring(index + separator.length());
            try {
                int number = Integer.parseInt(trailing);
                String base = s.substring(0, index + separator.length());
                
                do {
                    ++number;
                    s = base + number;
                } while (!isUniqueCallback.call(s));
                return s;
            } catch (NumberFormatException ex) {
            }
        }
        
        s = s + separator + "1";
        return getUniqueString(s, isUniqueCallback, separator);
    }
    
    
    /**
     * Determines if a string is unique via a callback, and if it is not, retrieves a string that is unique.
     * @param s The string of interest, not <code>null</code>.
     * @param isUniqueCallback  The callback, its {@link Callback#call(java.lang.Object) } should return <code>true</code>
     * if the argument is unique.
     * @return The unique string.
     */
    public static String getUniqueString(String s, Callback<String, Boolean> isUniqueCallback) {
        return getUniqueString(s, isUniqueCallback, null);
    }
    
    
    /**
     * Determines if a string is in a collection of strings, and if it is generates a new string
     * that is not in the collection.
     * @param s The string of interest.
     * @param existingStrings   The collection of existing strings.
     * @param separator The separator used to separate the original string from appended digits, if <code>null</code>
     * a default separator will be used.
     * @return The unique string.
     */
    public static String getUniqueString(String s, Collection<String> existingStrings, String separator) {
        return getUniqueString(s, (sNew) -> {
            return !existingStrings.contains(sNew);
        }, separator);
    }
    
    
    /**
     * Determines if a string is in a collection of strings, and if it is generates a new string
     * that is not in the collection.
     * @param s The string of interest.
     * @param existingStrings   The collection of existing strings.
     * @return The unique string.
     */
    public static String getUniqueString(String s, Collection<String> existingStrings) {
        return getUniqueString(s, existingStrings, null);
    }
}
