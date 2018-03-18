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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class CSVUtilTest {
    
    public CSVUtilTest() {
    }

    @Test
    public void testEncloseInQuotes() {
        String result = CSVUtil.encloseInQuotes("Abc");
        assertEquals("\"Abc\"", result);
        
        result = CSVUtil.encloseInQuotes("Abc\"Def");
        assertEquals("\"Abc\"\"Def\"", result);
        
        result = CSVUtil.encloseInQuotes("\"Abc\"\"Def\"");
        assertEquals("\"\"\"Abc\"\"\"\"Def\"\"\"", result);
    }
    
}
