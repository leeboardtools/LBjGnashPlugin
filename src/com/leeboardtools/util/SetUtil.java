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

import java.util.Set;

/**
 * Some utilities for working with {@link Set}s.
 * @author Albert Santos
 */
public class SetUtil {
    
    /**
     * This checks if two sets are equal, and if not, replaces the contents of one
     * set with the contents of the other set.
     * @param <T>   The set's type.
     * @param dstSet    The set to be copied to.
     * @param srcSet    The set to copy from, this is not modified.
     * @return <code>true</code> if dstSet was modified.
     */
    public static <T> boolean copySet(Set<T> dstSet, Set<T> srcSet) {
        if (dstSet.equals(srcSet)) {
            return false;
        }
        dstSet.clear();
        dstSet.addAll(srcSet);
        return true;
    }
}
