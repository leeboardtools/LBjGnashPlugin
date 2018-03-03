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
import java.util.Iterator;

/**
 * Interface for objects that support being similar, or "close enough".
 * @author Albert Santos
 */
public interface Similarable <T> {
    public boolean isSimilar(T other);
    
    
    public static <T extends Similarable> boolean areSimilar(T a, T b) {
        if (a == null) {
            return b == null;
        }
        else if (b == null) {
            return false;
        }
        return a.isSimilar(b);
    }
    
    public static <T extends Similarable> boolean areSimilar(Collection<T> a, Collection<T> b) {
        if (a == null) {
            return b == null;
        }
        else if (b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        
        return areSimilar(a.iterator(), b.iterator());
    }
    
    public static <T extends Similarable> boolean areSimilar(Iterator<T> a, Iterator<T> b) {
        if (a == null) {
            return b == null;
        }
        else if (b == null) {
            return false;
        }
        
        while (a.hasNext() && b.hasNext()) {
            if (!areSimilar(a.next(), b.next())) {
                return false;
            }
        }
        
        return a.hasNext() == b.hasNext();
    }
    
    public static <T extends Similarable> boolean areSimilar(T [] a, T [] b) {
        if (a == null) {
            return b == null;
        }
        else if (b == null) {
            return false;
        }
        if (a.length != b.length) {
            return false;
        }
        
        for (int i = 0; i < a.length; ++i) {
            if (!areSimilar(a[i], b[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    public static <T extends Similarable> T getSimilar(Collection<T> collection, T similarTo) {
        return getSimilar(collection.iterator(), similarTo);
    }
    
    public static <T extends Similarable> T getSimilar(Iterator<T> iterator, T similarTo) {
        while (iterator.hasNext()) {
            T object = iterator.next();
            if (object.isSimilar(similarTo)) {
                return object;
            }
        }
        return null;
    }
    
}
