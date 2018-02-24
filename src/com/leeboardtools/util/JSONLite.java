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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.util.Callback;

/**
 * My own little JSON reader/writer, just because there aren't enough implementations out there...
 * <p>
 * @author Albert Santos
 */
public class JSONLite {
    private static Callback<Void, JSONObject> objectCreator = (Void param) -> new JSONHashObject();
    private static ObjectNameHandling objectNameHandling = ObjectNameHandling.NAMES_UNIQUE;
    
    /**
     * Determines how JSON objects handle duplicate names.
     */
    public enum ObjectNameHandling {
        NAMES_UNIQUE,
        DUPLICATES_ALLOWED,
    }
    
    /**
     * @return The current setting determining how duplicate names are handled in JSON objects.
     */
    public static ObjectNameHandling getObjectNameHandling() {
        return objectNameHandling;
    }
    
    /**
     * Sets how duplicate names are handled in JSON objects. This only affects new readings
     * with {@link JSONReader} and creating of JSON objects via {@link #newJSONObject() }.
     * @param handling The handling to install.
     */
    public static void setObjectNameHandling(ObjectNameHandling handling) {
        if (objectNameHandling != handling) {
            Objects.requireNonNull(handling);
            objectNameHandling = handling;
            switch(objectNameHandling) {
                case NAMES_UNIQUE:
                    objectCreator = (Void param) -> new JSONHashObject();
                    break;
                    
                case DUPLICATES_ALLOWED:
                    objectCreator = (Void param) -> new JSONListObject();
                    break;
            }
        }
    }
    
    /**
     * Used to allocate new JSON objects based on the current {@link #getObjectNameHandling() } setting.
     * @return A new {@link JSONObject}.
     */
    public static JSONObject newJSONObject() {
        return objectCreator.call(null);
    }
    
    
    /**
     * Holds a name and a value together, used by {@link JSONObject}.
     */
    public static class NameValue {
        private final String name;
        private final JSONValue value;
        
        public NameValue(String name, JSONValue value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        public JSONValue getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.name);
            hash = 53 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NameValue other = (NameValue) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * Abstract base class for JSON objects, which is the entity enclosed in matched
     * '{' and '}' and containing name value pairs.
     */
    public static abstract class JSONObject implements Iterable<NameValue> {
        public JSONObject() {
        }
        
        /**
         * Removes all name-value pairs from the object.
         */
        public abstract void clear();
        
        /**
         * Adds a name-value pair to the object. If the object does not allow duplicate
         * names and an entry with the name already exists, the value of the existing
         * entry is replaced.
         * @param name  The name.
         * @param value     The value.
         */
        public abstract void add(String name, JSONValue value);
        
        /**
         * @return Retrieves a collection of the names of all the name-value pairs
         * in the object. This collection should NOT be modified.
         */
        public abstract Collection<String> getNames();
        
        /**
         * Retrieves a value associated with a name, if any. If duplicate names are allowed
         * and there are multiple entries for the name, which value is retrieved is 
         * up to the implementation.
         * @param name  The name of interest.
         * @return The value, <code>null</code> if the name is not part of the object.
         */
        public abstract JSONValue getValue(String name);
        
        @Override
        public abstract Iterator<NameValue> iterator();
        
        public final void add(String name, String value) {
            JSONObject.this.add(name, new JSONValue(value));
        }
        
        public final void add(String name, double value) {
            JSONObject.this.add(name, new JSONValue(value));
        }
        
        public final void add(String name, int value) {
            JSONObject.this.add(name, new JSONValue(value));
        }

        public final void add(String name, JSONObject value) {
            JSONObject.this.add(name, new JSONValue(value));
        }

        public final void add(String name, JSONValue [] value) {
            JSONObject.this.add(name, new JSONValue(value));
        }
        
        public final void add(String name, boolean value) {
            JSONObject.this.add(name, new JSONValue(value));
        }
        
        public final void add(String name) {
            JSONObject.this.add(name, new JSONValue());
        }
    }
    
    
    /**
     * A {@link HashMap} based implementation of {@link JSONObject}, this does not
     * allow duplicate names.
     */
    public static class JSONHashObject extends JSONObject {
        private final Map<String, JSONValue> nameValues = new HashMap<>();

        public JSONHashObject() {}

        @Override
        public void clear() {
            nameValues.clear();
        }
        
        @Override
        public void add(String name, JSONValue value) {
            nameValues.put(name, value);
        }
        
        @Override
        public Collection<String> getNames() {
            return nameValues.keySet();
        }
        
        @Override
        public JSONValue getValue(String name) {
            return nameValues.get(name);
        }
        
        @Override
        public Iterator<NameValue> iterator() {
            return new Iterator<NameValue>() {
                final Iterator<Map.Entry<String, JSONValue>> myIterator = nameValues.entrySet().iterator();
                
                @Override
                public boolean hasNext() {
                    return myIterator.hasNext();
                }

                @Override
                public NameValue next() {
                    Map.Entry<String, JSONValue> entry = myIterator.next();
                    return new NameValue(entry.getKey(), entry.getValue());
                }
                
            };
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.nameValues);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JSONHashObject other = (JSONHashObject) obj;
            if (!Objects.equals(this.nameValues, other.nameValues)) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * A {@link ArrayList} based implementation of {@link JSONObject}, this allows
     * duplicate names. It also maintains the order in which name-values are encountered.
     */
    public static class JSONListObject extends JSONObject {
        private final List<NameValue> nameValues = new ArrayList<>();

        public JSONListObject() {}

        @Override
        public void clear() {
            nameValues.clear();
        }
        
        @Override
        public void add(String name, JSONValue value) {
            nameValues.add(new NameValue(name, value));
        }
        
        @Override
        public Collection<String> getNames() {
            List<String> names = new ArrayList<>();
            nameValues.forEach((nameValue) -> {
                names.add(nameValue.getName());
            });
            return names;
        }
        
        @Override
        public JSONValue getValue(String name) {
            for (NameValue nameValue : nameValues) {
                if (nameValue.getName().equals(name)) {
                    return nameValue.getValue();
                }
            }
            return null;
        }
        
        @Override
        public Iterator<NameValue> iterator() {
            return nameValues.iterator();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.nameValues);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JSONListObject other = (JSONListObject) obj;
            if (!Objects.equals(this.nameValues, other.nameValues)) {
                return false;
            }
            return true;
        }
    }
    
    
    /**
     * The different types of JSON values.
     */
    public static enum ValueType {
        STRING,
        NUMBER,
        OBJECT,
        ARRAY,
        TRUE,
        FALSE,
        NULL,
    }
    
    /**
     * Represents a JSON value.
     */
    public static class JSONValue {
        private final ValueType valueType;
        private final String stringValue;
        private final double numberValue;
        private final JSONObject objectValue;
        private final JSONValue [] arrayValue;
        
        private JSONValue(ValueType type, String stringValue, double numberValue, JSONObject object, JSONValue [] array) {
            this.valueType = type;
            this.stringValue = stringValue;
            this.numberValue = numberValue;
            this.objectValue = object;
            this.arrayValue = array;
        }
        
        /**
         * Constructor for a JSON string value.
         * @param value The value, must not be <code>null</code>.
         */
        public JSONValue(String value) {
            this(ValueType.STRING, value, Double.NaN, null, null);
        }

        /**
         * Constructor for a JSON number value using a <code>double</code>.
         * @param value The value.
         */
        public JSONValue(double value) {
            this(ValueType.NUMBER, null, value, null, null);
        }

        /**
         * Constructor for a JSON number value using a <code>int</code>.
         * @param value The value.
         */
        public JSONValue(int value) {
            this(ValueType.NUMBER, null, value, null, null);
        }

        /**
         * Constructor for a JSON object value.
         * @param value The value, must not be <code>null</code>.
         */
        public JSONValue(JSONObject value) {
            this(ValueType.OBJECT, null, Double.NaN, value, null);
        }

        /**
         * Constructor for a JSON array value.
         * @param value The value, must not be <code>null</code>.
         */
        public JSONValue(JSONValue [] value) {
            this(ValueType.ARRAY, null, Double.NaN, null, value);
        }

        /**
         * Constructor for a JSON true or false value.
         * @param value The value.
         */
        public JSONValue(boolean value) {
            this((value) ? ValueType.TRUE : ValueType.FALSE, null, Double.NaN, null, null);
        }

        /**
         * Constructor for a JSON null value.
         */
        public JSONValue() {
            this(ValueType.NULL, null, Double.NaN, null, null);
        }
        
        /**
         * @return The value's type.
         */
        public final ValueType getValueType() {
            return valueType;
        }
        
        /**
         * @return A string value's string.
         * @throws JSONException if the value's type is not {@link ValueType#STRING}.
         */
        public final String getStringValue() {
            if (valueType != ValueType.STRING) {
                throw new JSONException("getValueType() is not ValueType.STRING!");
            }
            return this.stringValue;
        }
        
        /**
         * @return A number value's value as a double.
         * @throws JSONException if the value's type is not {@link ValueType#NUMBER}.
         */
        public final double getDoubleValue() {
            if (valueType != ValueType.NUMBER) {
                throw new JSONException("getValueType() is not ValueType.NUMBER!");
            }
            return this.numberValue;
        }
        
        /**
         * @return A number value's value as an int. The value is cast to an int from a double.
         * @throws JSONException if the value's type is not {@link ValueType#NUMBER}.
         */
        public final int getIntValue() {
            if (valueType != ValueType.NUMBER) {
                throw new JSONException("getValueType() is not ValueType.NUMBER!");
            }
            return (int)this.numberValue;
        }
        
        /**
         * @return A object value's object, not <code>null</code>.
         * @throws JSONException if the value's type is not {@link ValueType#OBJECT}.
         */
        public final JSONObject getObjectValue() {
            if (valueType != ValueType.OBJECT) {
                throw new JSONException("getValueType() is not ValueType.OBJECT!");
            }
            return this.objectValue;
        }
        
        /**
         * @return An array value's array, not <code>null</code>.
         * @throws JSONException if the value's type is not {@link ValueType#ARRAY}.
         */
        public final JSONValue [] getArrayValue() {
            if (valueType != ValueType.ARRAY) {
                throw new JSONException("getValueType() is not ValueType.ARRAY!");
            }
            return this.arrayValue;
        }
        
        /**
         * @return <code>true</code> if the value's type is {@link ValueType#TRUE}.
         */
        public final boolean isTrue() {
            return (valueType == ValueType.TRUE);
        }
        
        /**
         * @return <code>true</code> if the value's type is {@link ValueType#FALSE}.
         */
        public final boolean isFalse() {
            return (valueType == ValueType.FALSE);
        }
        
        /**
         * @return <code>true</code> if the value's type is {@link ValueType#NULL}.
         */
        public final boolean isNull() {
            return (valueType == ValueType.NULL);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + Objects.hashCode(this.valueType);
            hash = 29 * hash + Objects.hashCode(this.stringValue);
            hash = 29 * hash + (int) (Double.doubleToLongBits(this.numberValue) ^ (Double.doubleToLongBits(this.numberValue) >>> 32));
            hash = 29 * hash + Objects.hashCode(this.objectValue);
            hash = 29 * hash + Arrays.deepHashCode(this.arrayValue);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JSONValue other = (JSONValue) obj;
            
            if (this.valueType != other.valueType) {
                return false;
            }
            
            switch (this.valueType) {
                case STRING:
                    if (!this.stringValue.equals(other.stringValue)) {
                        return false;
                    }
                    break;
                    
                case NUMBER:
                    if (this.numberValue != other.numberValue) {
                        return false;
                    }
                    break;
                    
                case OBJECT:
                    if (!Objects.equals(this.objectValue, other.objectValue)) {
                        return false;
                    }
                    break;
                    
                case ARRAY:
                    if (!Arrays.deepEquals(this.arrayValue, other.arrayValue)) {
                        return false;
                    }
                    break;
                    
                case TRUE:
                    break;
                case FALSE:
                    break;
                case NULL:
                    break;
                default:
                    throw new AssertionError(this.valueType.name());
                
            }

            return true;
        }
    }
    
    
    /**
     * The exception we throw...
     */
    public static class JSONException extends RuntimeException {
        public JSONException(String msg) {
            super(msg);
        }
    }
    
    
    static class ScopeInfo {
        int itemCount = 0;
        ScopeInfo() {}
    }
    
    
    /**
     * Writes out JSON objects and arrays to a {@link Writer}.
     */
    public static class JSONWriter {
        private final Writer writer;
        private final List<ScopeInfo> scopeInfoStack = new ArrayList<>();
        private ScopeInfo activeScopeInfo;
        private String currentIndentString = "";
        private String arrayStart;
        private String arrayEnd;
        private String objectStart;
        private String objectEnd;
        private String nameValueSeparator;
        private String entrySeparator;
        private int indentAmount;
        private String spacesPerIndent;

        /**
         * Constructor.
         * @param writer    The writer to write to, not <code>null</code>.
         * @param indentAmount The amount to indent entries, set to 0 for the most compact.
         */
        public JSONWriter(Writer writer, int indentAmount) {
            Objects.requireNonNull(writer);
            this.writer = writer;
            pushIndent();
            setIndentAmount(indentAmount);
        }
        
        /**
         * Changes the indentation level.
         * @param indentAmount The amount to indent entries, set to 0 for the most compact.
         */
        public final void setIndentAmount(int indentAmount) {
            if (indentAmount <= 0) {
                this.indentAmount = 0;
                this.arrayStart = "[";
                this.arrayEnd = "]";
                this.objectStart = "{";
                this.objectEnd = "}";
                this.nameValueSeparator = ":";
                this.entrySeparator = ",";
                
                this.spacesPerIndent = null;
                this.currentIndentString = "";
            }
            else {
                this.indentAmount = indentAmount;
                this.arrayStart = "[\n";
                this.arrayEnd = "]";
                this.objectStart = "{\n";
                this.objectEnd = "}";
                this.nameValueSeparator = " : ";
                this.entrySeparator = ",\n";
            
                char [] charsPerIndent = new char [this.indentAmount];
                Arrays.fill(charsPerIndent, ' ');
                this.spacesPerIndent = new String(charsPerIndent);
                this.currentIndentString = "";
                for (int i = 1; i < this.scopeInfoStack.size(); ++i) {
                    this.currentIndentString += this.spacesPerIndent;
                }
            }
        }
        
        /**
         * Writes out a {@link JSONObject}.
         * @param object    The object to write.
         * @throws IOException on I/O errors.
         */
        public void writeJSONObject(JSONObject object) throws IOException {
            startObject();

            for (NameValue nameValue : object) {
                startNewEntry();
                writeName(nameValue.getName());
                writeJSONValue(nameValue.getValue());
            }

            endObject();
        }
        
        /**
         * Writes out a {@link JSONValue}.
         * @param value The value to write.
         * @throws IOException on I/O errors.
         */
        public void writeJSONValue(JSONValue value) throws IOException {
            switch (value.getValueType()) {
                case STRING :
                    writeJSONText(encodeToJSONString(value.getStringValue()));
                    break;

                case NUMBER:
                    final int intValue = value.getIntValue();
                    final double doubleValue = value.getDoubleValue();
                    if (intValue == doubleValue) {
                        writeJSONText(Integer.toString(intValue));
                    }
                    else {
                        writeJSONText(Double.toString(doubleValue));
                    }
                    break;

                case OBJECT:
                    writeJSONObject(value.getObjectValue());
                    break;

                case ARRAY:
                    writeJSONArray(value.getArrayValue());
                    break;

                case TRUE:
                    writeJSONText("true");
                    break;

                case FALSE:
                    writeJSONText("false");
                    break;

                case NULL:
                    writeJSONText("null");
                    break;

                default:
                    throw new AssertionError(value.getValueType().name());
            }
        }
        
        /**
         * Writes out a JSON array.
         * @param array The array to write.
         * @throws IOException on I/O errors.
         */
        public void writeJSONArray(JSONValue [] array) throws IOException {
            startArray();
            for (JSONValue arrayValue : array) {
                startNewEntry();
                writeJSONValue(arrayValue);
            }
            endArray();
        }

        void startArray() throws IOException {
            writer.append(arrayStart);
            pushIndent();
        }
        
        void endArray() throws IOException {
            popIndent();
            if (indentAmount > 0) {
                writer.append('\n');
                writer.append(currentIndentString);
            }
            writer.append(arrayEnd);
        }
        
        void startObject() throws IOException {
            writer.append(objectStart);
            pushIndent();
        }
        
        void endObject() throws IOException {
            popIndent();
            if (indentAmount > 0) {
                writer.append('\n');
                writer.append(currentIndentString);
            }
            writer.append(objectEnd);
        }
        
        void startNewEntry() throws IOException {
            if (activeScopeInfo.itemCount > 0) {
                writer.append(entrySeparator);
            }
            if (indentAmount > 0) {
                writer.append(currentIndentString);
            }
            ++activeScopeInfo.itemCount;
        }
        
        void writeName(String name) throws IOException {
            writer.append(encodeToJSONString(name));
            writer.append(nameValueSeparator);
        }
        
        void writeJSONText(String value) throws IOException {
            writer.append(value);
        }
        
        final void pushIndent() {
            activeScopeInfo = new ScopeInfo();
            scopeInfoStack.add(activeScopeInfo);
            if (indentAmount > 0) {
                currentIndentString += spacesPerIndent;
            }
        }
        final void popIndent() {
            scopeInfoStack.remove(scopeInfoStack.size() - 1);
            activeScopeInfo = scopeInfoStack.get(scopeInfoStack.size() - 1);
            if (indentAmount > 0) {
                currentIndentString = currentIndentString.substring(0, currentIndentString.length() - indentAmount);
            }
        }
    }
    
    
    /**
     * Determines if a Unicode code point is to be treated as white space according to the JSON spec.
     * @param codePoint The code point.
     * @return <code>true</code> if the code point is white space.
     */
    public static boolean isWhiteSpace(int codePoint) {
        switch (codePoint) {
            case 0x0009 :
            case 0x000A :
            case 0x000D :
            case 0x0020 :
                return true;
        }
        return false;
    }
    
    
    public static final String ESC_QUOTATION = "\\\"";
    public static final String ESC_REVERSE_SOLIDUS = "\\\\";
    public static final String ESC_SOLIDUS = "\\/";
    public static final String ESC_BACKSPACE = "\\b";
    public static final String ESC_FORM_FEED = "\\f";
    public static final String ESC_LINE_FEED = "\\n";
    public static final String ESC_CARRIAGE_RETURN = "\\r";
    public static final String ESC_TAB = "\\t";
    public static final String ESC_UNICODE_FOUR_HEX = "\\u";
    
    /**
     * Encodes a normal string into a JSON string.
     * @param plainString   The string to encode.
     * @return The encoded JSON string.
     */
    public static String encodeToJSONString(String plainString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('"');
        int length = plainString.length();
        for (int i = 0; i < length; ++i) {
            int codePoint = plainString.codePointAt(i);
            switch (codePoint) {
                case 0x0022 :
                    stringBuilder.append(ESC_QUOTATION);
                    break;
                    
                case 0x005C :
                    stringBuilder.append(ESC_REVERSE_SOLIDUS);
                    break;
                    
                case 0x002F :
                    stringBuilder.append(ESC_SOLIDUS);
                    break;
                    
                case 0x0008 :
                    stringBuilder.append(ESC_BACKSPACE);
                    break;
                    
                case 0x000C :
                    stringBuilder.append(ESC_FORM_FEED);
                    break;
                    
                case 0x000A :
                    stringBuilder.append(ESC_LINE_FEED);
                    break;
                    
                case 0x000D :
                    stringBuilder.append(ESC_CARRIAGE_RETURN);
                    break;
                    
                case 0x0009 :
                    stringBuilder.append(ESC_TAB);
                    break;
                    
                default :
                    if ((codePoint >= 0) && (codePoint <= 0x001F)) {
                        stringBuilder.append(ESC_UNICODE_FOUR_HEX);
                        stringBuilder.append(String.format("%04X", codePoint));
                    }
                    else {
                        stringBuilder.appendCodePoint(codePoint);
                    }
                    break;
            }
        }
        stringBuilder.append('"');
        return stringBuilder.toString();
    }
    
    /**
     * The token types.
     */
    public enum TokenType {
        ARRAY_START,
        ARRAY_END,
        OBJECT_START,
        OBJECT_END,
        STRING,
        NUMBER,
        TRUE,
        FALSE,
        NULL,
        COLON,
        COMMA,
        END,
    }
    
    /**
     * Determines the most likely token type represented by a code point, if any.
     * @param codePoint The code point.
     * @return The token type, <code>null</code> if the code point cannot represent a token.
     */
    public static TokenType codePointToTokenType(int codePoint) {
        switch (codePoint) {
            case '[' :
                return TokenType.ARRAY_START;
            case ']' :
                return TokenType.ARRAY_END;
            case '{' :
                return TokenType.OBJECT_START;
            case '}' :
                return TokenType.OBJECT_END;
            case '"' :
                return TokenType.STRING;
            case 't' :
                return TokenType.TRUE;
            case 'f' :
                return TokenType.FALSE;
            case 'n' :
                return TokenType.NULL;
            case ':' :
                return TokenType.COLON;
            case ',' :
                return TokenType.COMMA;
            case -1 :
                return TokenType.END;
                
            case '-' :
                return TokenType.NUMBER;
                
            default :
                if (codePointToDigit(codePoint) >= 0) {
                    return TokenType.NUMBER;
                }
                break;
        }
        
        return null;
    }
    
    /**
     * Convertes a Unicode code point to a digit according to JSON's rules.
     * @param codePoint The code point.
     * @return The digit equivalent between 0 and 9 inclusive, -1 if the code point is
     * not a valid digit.
     */
    public static int codePointToDigit(int codePoint) {
        return (codePoint >= 0x0030) && (codePoint <= 0x0039) ? (codePoint - 0x0030) : -1;
    }
        
    
    /**
     * Reader for reading JSON objects, arrays, and values from a {@link Reader}.
     */
    public static class JSONReader {
        private final BufferedReader reader;
        private TokenType tokenType;
        private String tokenString;
        private double tokenNumber;
        private int cachedCodePoint;
        private int charactersRead;
        private int linesRead;
        private int charactersReadThisLine;
        
        /**
         * Constructor.
         * @param reader    The reader.
         */
        public JSONReader(Reader reader) {
            if (reader instanceof BufferedReader) {
                this.reader = (BufferedReader)reader;
            }
            else {
                this.reader = new BufferedReader(reader);
            }
        }
        
        /**
         * Constructor.
         * @param text The text to be read.
         */
        public JSONReader(String text) {
            this(new StringReader(text));
        }
        
        /**
         * Constructor.
         * @param stream The input stream to be read.
         */
        public JSONReader(InputStream stream) {
            this (new InputStreamReader(stream));
        }
        
        static enum ObjectState {
            OPENING_BRACE,
            NAME_OR_CLOSING_BRACE,
            NAME,
            COLON,
            VALUE,
            COMMA_OR_CLOSING_BRACE,
            DONE,
        }
        
        
        /**
         * Reads a {@link JSONObject}.
         * @return  The object.
         * @throws IOException on I/O errors.
         * @throws JSONException on parsing errors.
         */
        public JSONObject readJSONObject() throws IOException {
            JSONObject object = newJSONObject();
            
            ObjectState state = ObjectState.OPENING_BRACE;
            String currentName = null;
            
            while (true) {
                switch (state) {
                    case OPENING_BRACE :
                        if (getTokenType() != TokenType.OBJECT_START) {
                            throwException("Invalid object, opening '{' not encountered.");
                        }
                        state = ObjectState.NAME_OR_CLOSING_BRACE;
                        advanceToken();
                        break;
                        
                    case NAME_OR_CLOSING_BRACE :
                    case NAME:
                        if (getTokenType() == TokenType.OBJECT_END) {
                            if (state != ObjectState.NAME_OR_CLOSING_BRACE) {
                                throwException("Invalid object, closing '}' not allowed after a ','.");
                            }
                            advanceToken();
                            state = ObjectState.DONE;
                            break;
                        }
                        if (getTokenType() != TokenType.STRING) {
                            throwException("Invalid object, expected 'name'.");
                        }
                        currentName = getTokenString();
                        state = ObjectState.COLON;
                        advanceToken();
                        break;
                        
                    case COLON:
                        if (getTokenType() != TokenType.COLON) {
                            throwException("Invalid object, expected ':' after the 'name'.");
                        }
                        state = ObjectState.VALUE;
                        advanceToken();
                        break;
                        
                    case VALUE:
                        JSONValue value = readJSONValue();
                        object.add(currentName, value);
                        state = ObjectState.COMMA_OR_CLOSING_BRACE;
                        break;
                        
                    case COMMA_OR_CLOSING_BRACE:
                        switch (getTokenType()) {
                            case COMMA :
                                state = ObjectState.NAME;
                                break;
                                
                            case OBJECT_END :
                                state = ObjectState.DONE;
                                break;
                                
                            default :
                                throwException("Invalid object, expected either ',' or '}'.");
                                break;
                        }
                        advanceToken();
                        break;
                        
                    case DONE:
                        return object;
                        
                    default:
                        throw new AssertionError(state.name());
                }
            }
        }
        
        
        
        /**
         * Reads a {@link JSONValue}.
         * @return  The value.
         * @throws IOException on I/O errors.
         * @throws JSONException on parsing errors.
         */
        public JSONValue readJSONValue() throws IOException {
            JSONValue value = null;
            switch (getTokenType()) {
                case ARRAY_START :
                    JSONValue [] values = readJSONArray();
                    return new JSONValue(values);
                    
                case OBJECT_START :
                    JSONObject object = readJSONObject();
                    return new JSONValue(object);
                    
                case STRING :
                    value = new JSONValue(getTokenString());
                    break;
                    
                case NUMBER :
                    value = new JSONValue(getTokenNumber());
                    break;
                    
                case TRUE :
                    value = new JSONValue(true);
                    break;
                    
                case FALSE :
                    value = new JSONValue(false);
                    break;
                    
                case NULL :
                    value = new JSONValue();
                    break;
                    
                default :
                    throwException("Invalid value, expected a string in '\"', a number, 'true', 'false', or 'null'.");
                    
            }
            advanceToken();
            
            return value;
        }
        
        
        static enum ArrayState {
            OPENING_BRACKET,
            VALUE_OR_CLOSING_BRACKET,
            VALUE,
            COMMA_OR_CLOSING_BRACKET,
            DONE
        }
        
        
        /**
         * Reads a JSON array.
         * @return  The array.
         * @throws IOException on I/O errors.
         * @throws JSONException on parsing errors.
         */
        public JSONValue [] readJSONArray() throws IOException {
            ArrayState state = ArrayState.OPENING_BRACKET;
            List<JSONValue> values = new ArrayList<>();
            
            while (true) {
                switch (state) {
                    case OPENING_BRACKET :
                        if (getTokenType() != TokenType.ARRAY_START) {
                            throwException("Invalid array, expected '['.");
                        }
                        state = ArrayState.VALUE_OR_CLOSING_BRACKET;
                        advanceToken();
                        break;
                        
                    case VALUE_OR_CLOSING_BRACKET:
                    case VALUE:
                        if (getTokenType() == TokenType.ARRAY_END) {
                            if (state != ArrayState.VALUE_OR_CLOSING_BRACKET) {
                                throwException("Invalid array, expected a value after ','.");
                            }
                            advanceToken();
                            return values.toArray(new JSONValue[values.size()]);
                        }
                        values.add(readJSONValue());
                        state = ArrayState.COMMA_OR_CLOSING_BRACKET;
                        break;
                        
                    case COMMA_OR_CLOSING_BRACKET:
                        if (getTokenType() == TokenType.ARRAY_END) {
                            advanceToken();
                            return values.toArray(new JSONValue[values.size()]);
                        }
                        else if (getTokenType() != TokenType.COMMA) {
                            throwException("Invalid array, expected ',' or ']'.");
                        }
                        state = ArrayState.VALUE;
                        advanceToken();
                        break;
                        
                    case DONE:
                        break;
                        
                    default:
                        throw new AssertionError(state.name());
                }
            }
        }
        
        // Want to have the ability to read individual elements of an array.
        
        
        // Public so we could do something like detect arrays.
        public final TokenType getTokenType() throws IOException  {
            if (this.tokenType == null) {
                advanceToken();
            }
            return this.tokenType;
        }
        
        public final String getTokenString() {
            return this.tokenString;
        }
        
        public final double getTokenNumber() {
            return this.tokenNumber;
        }
        
        
        final boolean advanceToken() throws IOException {
            // Skip white space...
            if (cachedCodePoint == 0) {
                readCodePoint();
            }
            
            while (isWhiteSpace(cachedCodePoint)) {
                readCodePoint();
            }
            
            int codePoint = cachedCodePoint;
            cachedCodePoint = 0;
            
            this.tokenType = codePointToTokenType(codePoint);
            if (this.tokenType == null) {
                throwException("Invalid token encountered.");
            }
            switch (this.tokenType) {
                case STRING :
                    readJSONString();
                    break;
                    
                case NUMBER :
                    readJSONNumber(codePoint);
                    break;
                    
                case TRUE :
                    if ((readCodePoint() != 'r')
                     || (readCodePoint() != 'u')
                     || (readCodePoint() != 'e')) {
                        throwException("Invalid value, expected 'true'.");
                    }
                    this.cachedCodePoint = 0;
                    break;
                    
                case FALSE :
                    if ((readCodePoint() != 'a')
                     || (readCodePoint() != 'l')
                     || (readCodePoint() != 's')
                     || (readCodePoint() != 'e')) {
                        throwException("Invalid value, expected 'false'.");
                    }
                    this.cachedCodePoint = 0;
                    break;
                    
                case NULL :
                    if ((readCodePoint() != 'u')
                     || (readCodePoint() != 'l')
                     || (readCodePoint() != 'l')) {
                        throwException("Invalid value, expected 'null'.");
                    }
                    this.cachedCodePoint = 0;
                    break;
            }
            
            return true;
        }
        
        final void readJSONString() throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            readCodePoint();
            while (cachedCodePoint != '"') {
                if (cachedCodePoint == -1) {
                    throwException("Unterminated string encountered, end of file reached.");
                }
                if (cachedCodePoint == '\\') {
                    cachedCodePoint = readCodePoint();
                    switch (cachedCodePoint) {
                        case -1 :
                            throwException("Unterminated string encountered in escape sequence, end of file reached.");
                            
                        case '"' :
                        case '\\' :
                        case '/' :
                            stringBuilder.appendCodePoint(cachedCodePoint);
                            break;
                            
                        case 'b' :
                            stringBuilder.append('\b');
                            break;
                        case 'f' :
                            stringBuilder.append('\f');
                            break;
                        case 'n' :
                            stringBuilder.append('\n');
                            break;
                        case 'r' :
                            stringBuilder.append('\r');
                            break;
                        case 't' :
                            stringBuilder.append('\t');
                            break;
                            
                        case 'u' :
                            int value = 0;
                            for (int i = 0; i < 4; ++i) {
                                value *= 16;
                                int codePoint = readCodePoint();
                                if (codePoint == -1) {
                                    throwException("End of file reached reading '\\u' escape sequence.");
                                }
                                
                                int digit = codePointToDigit(codePoint);
                                if (digit >= 0) {
                                    value += digit;
                                }
                                else if ((codePoint >= 0x0041) && (codePoint <= 0x0046)) {
                                    value += 10 + (codePoint - 0x0041);
                                }
                                else if ((codePoint >= 0x0061) && (codePoint <= 0x0066)) {
                                    value += 10 + (codePoint - 0x0061);
                                }
                                else {
                                    throwException("Invalid hexadecimal character in the '\\u' escape sequence encountered.");
                                }
                            }
                            stringBuilder.appendCodePoint(value);
                            break;
                            
                        default :
                            throwException("Invalid string escape character encountered.");
                    }
                }
                else if ((cachedCodePoint >= 0) && (cachedCodePoint <= 0x01F)) {
                    throwException("Invalid code point encountered in string, control characters must be escaped.");
                }
                else {
                    stringBuilder.appendCodePoint(cachedCodePoint);
                }
                
                readCodePoint();
            }
            cachedCodePoint = 0;
            
            tokenString = stringBuilder.toString();
        }
        
        
        final void readJSONNumber(int firstCodePoint) throws IOException {
            double value = 0;
            int valuePower10 = 0;
            int exponentSign = 1;
            int exponent = 0;
            
            double valueSign = 1.;
            int codePoint = firstCodePoint;
            if (codePoint == '-') {
                valueSign = -1.;
                codePoint = readCodePoint();
            }
            
            int digit = codePointToDigit(codePoint);
            if (digit < 0) {
                throwException("Invalid number, a leading digit was expected.");
            }
            else if (digit > 0) {
                // Only a single leading digit is allowed.
                while (digit >= 0) {
                    value *= 10.;
                    value += digit;
                    codePoint = readCodePoint();
                    digit = codePointToDigit(codePoint);
                }
            }
            else {
                codePoint = readCodePoint();
            }
            
            if (codePoint == '.') {
                codePoint = readCodePoint();
                digit = codePointToDigit(codePoint);
                if (digit <= 0) {
                    throwException("Invalid number, a digit is required after the decimal.");
                }
                while (digit >= 0) {
                    value *= 10;
                    value += digit;
                    ++valuePower10;
                    
                    codePoint = readCodePoint();
                    digit = codePointToDigit(codePoint);
                }
            }
            
            if ((codePoint == 'e') || (codePoint == 'E')) {
                codePoint = readCodePoint();
                
                if (codePoint == '+') {
                    codePoint = readCodePoint();
                }
                else if (codePoint == '-') {
                    codePoint = readCodePoint();
                    exponentSign = -1;
                }
                
                digit = codePointToDigit(codePoint);
                if (codePoint == -1) {
                    throwException("End of file reached reading the exponent.");
                }
                else if (digit < 0) {
                    throwException("Invalid number, one or more digits are required for the exponent.");
                }
                while (digit >= 0) {
                    exponent *= 10;
                    exponent += digit;
                    
                    codePoint = readCodePoint();
                    digit = codePointToDigit(codePoint);
                }
            }
            
            while (isWhiteSpace(codePoint)) {
                codePoint = readCodePoint();
            }
            
            TokenType nextTokenType = codePointToTokenType(codePoint);
            if (nextTokenType == null) {
                throwException("Invalid number, an invalid code point was encountered at the end of the number.");
            }
            
            switch (nextTokenType) {
                case COMMA :
                case ARRAY_END :
                case OBJECT_END :
                case END :
                    break;
                   
                default :
                    throwException("Invalid number, an invalid code point was encountered at the end of the number.");
            }
            
            exponent *= exponentSign;
            exponent -= valuePower10;
            
            if (exponent < Double.MIN_EXPONENT) {
                throwException("Invalid number, the exponent is too small.");
            }
            else if (exponent > Double.MAX_EXPONENT) {
                throwException("Invalid number, the exponent is too large.");
            }
            
            this.tokenNumber = valueSign * value;
            if (exponent != 0) {
                this.tokenNumber *= Math.pow(10., exponent);
            }
        }
        
        final int readCodePoint() throws IOException {
            cachedCodePoint = this.reader.read();
            if (cachedCodePoint != -1) {
                if (cachedCodePoint == '\n') {
                    ++this.linesRead;
                    this.charactersReadThisLine = 0;
                }
                ++this.charactersRead;
                ++this.charactersReadThisLine;
            }
            
            return cachedCodePoint;
        }
        
        final void throwException(String message) {
            throw new JSONException(message + " Line: " + this.linesRead + "  Col: " + (this.charactersReadThisLine + 1));
        }
    }
}
