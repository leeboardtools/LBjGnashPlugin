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

import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class StringUtilTest {
    
    public StringUtilTest() {
    }

    @Test
    public void testGetUniqueString() {
        String result;
        HashSet<String> existingStrings = new HashSet<>();
        existingStrings.add("Abc");
        existingStrings.add("Def - 1");
        existingStrings.add("Def - 2");
        
        result = StringUtil.getUniqueString("Abc", existingStrings);
        assertEquals("Abc - 1", result);
        
        result = StringUtil.getUniqueString("Abc1", existingStrings);
        assertEquals("Abc1", result);
        
        result = StringUtil.getUniqueString("Def - 1", existingStrings);
        assertEquals("Def - 3", result);
    }
    
}
