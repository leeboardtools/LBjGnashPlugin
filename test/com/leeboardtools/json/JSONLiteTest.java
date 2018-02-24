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
package com.leeboardtools.json;

import com.leeboardtools.json.JSONLite;
import com.leeboardtools.json.JSONObject;
import com.leeboardtools.json.JSONReader;
import com.leeboardtools.json.JSONValue;
import com.leeboardtools.json.JSONWriter;
import com.leeboardtools.json.ParsingException;
import java.io.IOException;
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
        
        JSONObject object = JSONLite.newJSONObject();
        object.add("Abc", 123);
        
        JSONReader jsonReader;
        
        JSONWriter jsonWriter = new JSONWriter(writer, 0);
        jsonWriter.writeJSONObject(object);
        
        System.out.println("Single Abc:123");
        System.out.println(writer.toString());
        
        assertEquals("{\"Abc\":123}", writer.toString());
        
        jsonReader = new JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());
        
        
        writer.getBuffer().delete(0, writer.getBuffer().length());
        
        object.add("Def", true);
        object.add("Ghi", "Text");
        object.add("Jkl", 123.456);
        jsonWriter.writeJSONObject(object);
        
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("{\"Abc\":123,\"Def\":true,\"Ghi\":\"Text\",\"Jkl\":123.456}", writer.toString());
        
        jsonReader = new JSONReader(writer.toString());
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
        
        jsonReader = new JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());
        

        
        JSONObject object2 = JSONLite.newJSONObject();
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
        
        jsonReader = new JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());

        
        object.clear();
        JSONValue [] array = new JSONValue [] {
            new JSONValue(123)
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
        
        jsonReader = new JSONReader(writer.toString());
        assertEquals(object, jsonReader.readJSONObject());
    }
    
    static final double TOLERANCE = 1e-10;
    
    void assertValue(double refValue, JSONValue value) {
        assertEquals(JSONValue.ValueType.NUMBER, value.getValueType());
        assertEquals(refValue, value.getDoubleValue(), TOLERANCE);
    }

    void assertValue(int refValue, JSONValue value) {
        assertEquals(JSONValue.ValueType.NUMBER, value.getValueType());
        assertEquals(refValue, value.getIntValue());
    }
    
    void assertValue(String refValue, JSONValue value) {
        assertEquals(JSONValue.ValueType.STRING, value.getValueType());
        assertEquals(refValue, value.getStringValue());
    }
    void assertValue(boolean refValue, JSONValue value) {
        if (refValue) {
            assertEquals(JSONValue.ValueType.TRUE, value.getValueType());
            assertTrue(value.isTrue());
        }
        else {
            assertEquals(JSONValue.ValueType.FALSE, value.getValueType());
            assertTrue(value.isFalse());
        }
    }
    void assertValueNull(JSONValue value) {
        assertEquals(JSONValue.ValueType.NULL, value.getValueType());
        assertTrue(value.isNull());
    }
    
    void assertFailReadValue(JSONReader reader) {
        try {
            reader.readJSONValue();
        }
        catch (ParsingException | IOException ex) {
            return;
        }
        fail("Expected JSONLite.JSONException");
    }


    @Test
    public void testReadJSONNumber() throws IOException {
        JSONReader reader;
        JSONValue value;
        reader = new JSONReader("123");
        value = reader.readJSONValue();
        assertValue(123, value);
        
        reader = new JSONReader("0");
        value = reader.readJSONValue();
        assertValue(0, value);
        
        reader = new JSONReader("-0");
        value = reader.readJSONValue();
        assertValue(-0, value);
        
        reader = new JSONReader("-123");
        value = reader.readJSONValue();
        assertValue(-123, value);
        
        reader = new JSONReader("1.23");
        value = reader.readJSONValue();
        assertValue(1.23, value);
        
        reader = new JSONReader("-1.23");
        value = reader.readJSONValue();
        assertValue(-1.23, value);
        
        reader = new JSONReader("-12.3");
        value = reader.readJSONValue();
        assertValue(-12.3, value);
        
        reader = new JSONReader("-1.23e1");
        value = reader.readJSONValue();
        assertValue(-1.23e1, value);
        
        reader = new JSONReader("1.23e-10");
        value = reader.readJSONValue();
        assertValue(1.23e-10, value);
        
        reader = new JSONReader("-0.23E-123");
        value = reader.readJSONValue();
        assertValue(-0.23e-123, value);

        
        reader = new JSONReader("-00.23E-123");
        assertFailReadValue(reader);
        
        reader = new JSONReader("-1.E-123");
        assertFailReadValue(reader);
        
        reader = new JSONReader("-1.23a");
        assertFailReadValue(reader);
        
        reader = new JSONReader("a");
        assertFailReadValue(reader);
    }
    
    @Test
    public void testReadJSONString() throws Exception {
        JSONReader reader;
        JSONValue value;
        reader = new JSONReader("\"\"");
        value = reader.readJSONValue();
        assertValue("", value);

        reader = new JSONReader("   \" \\r \\n \\\" \\\\ \\/ \\b \\f \\t \"   ");
        value = reader.readJSONValue();
        assertValue(" \r \n \" \\ / \b \f \t ", value);

        reader = new JSONReader("\" \\uD834\\uDD1E \"");
        value = reader.readJSONValue();
        assertValue(" \uD834\uDD1E ", value);
        
        reader = new JSONReader("\" \n \"");
        assertFailReadValue(reader);
    }
    
    @Test
    public void testReadJSONMiscValues() throws Exception {
        JSONReader reader;
        JSONValue value;
        reader = new JSONReader(" \ntrue  ");
        value = reader.readJSONValue();
        assertValue(true, value);

        reader = new JSONReader(" false  ");
        value = reader.readJSONValue();
        assertValue(false, value);

        reader = new JSONReader("null,");
        value = reader.readJSONValue();
        assertValueNull(value);
    }

    
    void checkReader(JSONValue refArray [], JSONReader reader) throws IOException {
        JSONValue [] testArray = reader.readJSONArray();
        Assert.assertArrayEquals(refArray, testArray);
    }

    @Test
    public void testWriteReadJSONArray() throws Exception {
        StringWriter writer = new StringWriter();
        JSONWriter jsonWriter = new JSONWriter(writer, 0);
        JSONReader jsonReader;
        
        JSONValue array[] = new JSONValue [] {};
        
        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[]", writer.toString());
        
        jsonReader = new JSONReader(writer.toString());
        checkReader(array, jsonReader);
        
        
        array = new JSONValue [] {
            new JSONValue("Abc")
        };
        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[\"Abc\"]", writer.toString());
        
        jsonReader = new JSONReader(writer.toString());
        checkReader(array, jsonReader);

        
        array = new JSONValue[] {
            new JSONValue("Abc"),
            new JSONValue(123),
        };

        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[\"Abc\",123]", writer.toString());
        
        jsonReader = new JSONReader(writer.toString());
        checkReader(array, jsonReader);
        

        JSONObject object = JSONLite.newJSONObject();
        object.add("Def", -123);
        
        array = new JSONValue[] {
            new JSONValue("Abc"),
            new JSONValue(object),
            new JSONValue(123),
        };

        writer.getBuffer().delete(0, writer.getBuffer().length());
        jsonWriter.writeJSONArray(array);
        System.out.println("\nNextTest:");
        System.out.println(writer.toString());
        assertEquals("[\"Abc\",{\"Def\":-123},123]", writer.toString());
        
        jsonReader = new JSONReader(writer.toString());
        checkReader(array, jsonReader);
        
        
        jsonWriter.setIndentAmount(2);
        
        object.add("Ghi", new JSONValue [] { new JSONValue(true), new JSONValue() });
        
        array = new JSONValue[] {
            new JSONValue("Abc"),
            new JSONValue(object),
            new JSONValue(123),
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
        
        jsonReader = new JSONReader(writer.toString());
        checkReader(array, jsonReader);
    }

    
}
