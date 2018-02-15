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
 * Some helpers for working with arrays.
 * @author Albert Santos
 */
public class ArrayUtil {
    /**
     * Joins together several arrays into a single array.
     * @param args  The arrays to join together.
     * @return The single array.
     */
    public static int [] join(int [] ... args) {
        int length = 0;
        for (int [] array : args) {
            length += array.length;
        }
        int [] newArray = new int [length];
        
        int dstIndex = 0;
        for (int [] array : args) {
            System.arraycopy(array, 0, newArray, dstIndex, array.length);
            dstIndex += array.length;
        }
        return newArray;
    }
}
