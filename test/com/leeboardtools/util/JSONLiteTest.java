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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class JSONLiteTest {
    
    public JSONLiteTest() {
    }

    @Test
    public void testReadJSONObject() {
    }

    @Test
    public void testWriteReadJSONObject() throws Exception {
        StringWriter writer = new StringWriter();
        
        JSONLite.JSONObject object = JSONLite.newJSONObject();
        object.add("Abc", 123);
        
        JSONLite.JSONReader jsonReader;
        
        JSONLite.JSONWriter jsonWriter = new JSONLite.JSONWriter(writer, 0);
        jsonWriter.writeJSONObject(object);
        
        System.out.println("Single Abc:123");
        System.out.println(writer.toString());
        
        assertEquals("{\"Abc\":123}", writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());
        
        
        writer.getBuffer().delete(0, writer.getBuffer().length());
        
        object.add("Def", true);
        object.add("Ghi", "Text");
        object.add("Jkl", 123.456);
        jsonWriter.writeJSONObject(object);
        
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("{\"Abc\":123,\"Def\":true,\"Ghi\":\"Text\",\"Jkl\":123.456}", writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());

        
        jsonWriter.setIndentAmount(4);
        
        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONObject(object);

        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        
        assertEquals("{\n"
                + "    \"Abc\" : 123,\n"
                + "    \"Def\" : true,\n"
                + "    \"Ghi\" : \"Text\",\n"
                + "    \"Jkl\" : 123.456\n"
                + "}", 
                writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());
        

        
        JSONLite.JSONObject object2 = JSONLite.newJSONObject();
        object2.add("123");
        object.add("Bcd", object2);

        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONObject(object);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("{\n"
                + "    \"Bcd\" : {\n"
                + "        \"123\" : null\n"
                + "    },\n"
                + "    \"Abc\" : 123,\n"
                + "    \"Def\" : true,\n"
                + "    \"Ghi\" : \"Text\",\n"
                + "    \"Jkl\" : 123.456\n"
                + "}",
                writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());

        
        object.clear();
        JSONLite.JSONValue [] array = new JSONLite.JSONValue [] {
            new JSONLite.JSONValue(123)
        };
        object.add("Array", array);
        
        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONObject(object);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        
        assertEquals("{\n"
                + "    \"Array\" : [\n"
                + "        123\n"
                + "    ]\n"
                + "}",
                writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());
    }
    
    static final double TOLERANCE = 1e-10;
    
    void assertValue(double refValue, JSONLite.JSONValue value) {
        assertEquals(JSONLite.ValueType.NUMBER, value.getValueType());
        assertEquals(refValue, value.getDoubleValue(), TOLERANCE);
    }

    void assertValue(int refValue, JSONLite.JSONValue value) {
        assertEquals(JSONLite.ValueType.NUMBER, value.getValueType());
        assertEquals(refValue, value.getIntValue());
    }
    
    void assertValue(String refValue, JSONLite.JSONValue value) {
        assertEquals(JSONLite.ValueType.STRING, value.getValueType());
        assertEquals(refValue, value.getStringValue());
    }
    void assertValue(boolean refValue, JSONLite.JSONValue value) {
        if (refValue) {
            assertEquals(JSONLite.ValueType.TRUE, value.getValueType());
            assertTrue(value.isTrue());
        }
        else {
            assertEquals(JSONLite.ValueType.FALSE, value.getValueType());
            assertTrue(value.isFalse());
        }
    }
    void assertValueNull(JSONLite.JSONValue value) {
        assertEquals(JSONLite.ValueType.NULL, value.getValueType());
        assertTrue(value.isNull());
    }
    
    void assertFailReadValue(JSONLite.JSONReader reader) {
        try {
            reader.readJSONValue();
        }
        catch (JSONLite.JSONException | IOException ex) {
            return;
        }
        fail("Expected JSONLite.JSONException");
    }


    @Test
    public void testReadJSONNumber() throws IOException {
        JSONLite.JSONReader reader;
        JSONLite.JSONValue value;
        reader = new JSONLite.JSONReader("123");
        value = reader.readJSONValue();
        assertValue(123, value);
        
        reader = new JSONLite.JSONReader("0");
        value = reader.readJSONValue();
        assertValue(0, value);
        
        reader = new JSONLite.JSONReader("-0");
        value = reader.readJSONValue();
        assertValue(-0, value);
        
        reader = new JSONLite.JSONReader("-123");
        value = reader.readJSONValue();
        assertValue(-123, value);
        
        reader = new JSONLite.JSONReader("1.23");
        value = reader.readJSONValue();
        assertValue(1.23, value);
        
        reader = new JSONLite.JSONReader("-1.23");
        value = reader.readJSONValue();
        assertValue(-1.23, value);
        
        reader = new JSONLite.JSONReader("-12.3");
        value = reader.readJSONValue();
        assertValue(-12.3, value);
        
        reader = new JSONLite.JSONReader("-1.23e1");
        value = reader.readJSONValue();
        assertValue(-1.23e1, value);
        
        reader = new JSONLite.JSONReader("1.23e-10");
        value = reader.readJSONValue();
        assertValue(1.23e-10, value);
        
        reader = new JSONLite.JSONReader("-0.23E-123");
        value = reader.readJSONValue();
        assertValue(-0.23e-123, value);

        
        reader = new JSONLite.JSONReader("-00.23E-123");
        assertFailReadValue(reader);
        
        reader = new JSONLite.JSONReader("-1.E-123");
        assertFailReadValue(reader);
        
        reader = new JSONLite.JSONReader("-1.23a");
        assertFailReadValue(reader);
        
        reader = new JSONLite.JSONReader("a");
        assertFailReadValue(reader);
    }
    
    @Test
    public void testReadJSONString() throws Exception {
        JSONLite.JSONReader reader;
        JSONLite.JSONValue value;
        reader = new JSONLite.JSONReader("\"\"");
        value = reader.readJSONValue();
        assertValue("", value);

        reader = new JSONLite.JSONReader("   \" \\r \\n \\\" \\\\ \\/ \\b \\f \\t \"   ");
        value = reader.readJSONValue();
        assertValue(" \r \n \" \\ / \b \f \t ", value);

        reader = new JSONLite.JSONReader("\" \\uD834\\uDD1E \"");
        value = reader.readJSONValue();
        assertValue(" \uD834\uDD1E ", value);
        
        reader = new JSONLite.JSONReader("\" \n \"");
        assertFailReadValue(reader);
    }
    
    @Test
    public void testReadJSONMiscValues() throws Exception {
        JSONLite.JSONReader reader;
        JSONLite.JSONValue value;
        reader = new JSONLite.JSONReader(" \ntrue  ");
        value = reader.readJSONValue();
        assertValue(true, value);

        reader = new JSONLite.JSONReader(" false  ");
        value = reader.readJSONValue();
        assertValue(false, value);

        reader = new JSONLite.JSONReader("null,");
        value = reader.readJSONValue();
        assertValueNull(value);
    }

    
    void checkReader(JSONLite.JSONValue refArray [], JSONLite.JSONReader reader) throws IOException {
        JSONLite.JSONValue [] testArray = reader.readJSONArray();
        Assert.assertArrayEquals(refArray, testArray);
    }

    @Test
    public void testWriteReadJSONArray() throws Exception {
        StringWriter writer = new StringWriter();
        JSONLite.JSONWriter jsonWriter = new JSONLite.JSONWriter(writer, 0);
        JSONLite.JSONReader jsonReader;
        
        JSONLite.JSONValue array[] = new JSONLite.JSONValue [] {};
        
        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[]", writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        checkReader(array, jsonReader);
        
        
        array = new JSONLite.JSONValue [] {
            new JSONLite.JSONValue("Abc")
        };
        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[\"Abc\"]", writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        checkReader(array, jsonReader);

        
        array = new JSONLite.JSONValue[] {
            new JSONLite.JSONValue("Abc"),
            new JSONLite.JSONValue(123),
        };

        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[\"Abc\",123]", writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        checkReader(array, jsonReader);
        

        JSONLite.JSONObject object = JSONLite.newJSONObject();
        object.add("Def", -123);
        
        array = new JSONLite.JSONValue[] {
            new JSONLite.JSONValue("Abc"),
            new JSONLite.JSONValue(object),
            new JSONLite.JSONValue(123),
        };

        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[\"Abc\",{\"Def\":-123},123]", writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        checkReader(array, jsonReader);
        
        
        jsonWriter.setIndentAmount(2);
        
        object.add("Ghi", new JSONLite.JSONValue [] { new JSONLite.JSONValue(true), new JSONLite.JSONValue() });
        
        array = new JSONLite.JSONValue[] {
            new JSONLite.JSONValue("Abc"),
            new JSONLite.JSONValue(object),
            new JSONLite.JSONValue(123),
        };

        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[\n"
                + "  \"Abc\",\n"
                + "  {\n"
                + "    \"Def\" : -123,\n"
                + "    \"Ghi\" : [\n"
                + "      true,\n"
                + "      null\n"
                + "    ]\n"
                + "  },\n"
                + "  123\n"
                + "]", writer.toString());
        
        jsonReader = new JSONLite.JSONReader(writer.toString());
        checkReader(array, jsonReader);
    }

    
}
